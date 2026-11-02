package learning_management_system.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates the database schema on first start: when schema {@code lms_core} has no {@code users} table, the domain SQL
 * files in {@code db/schema} run in the order of {@code run_all.sql}, in one transaction. An existing schema is never
 * touched. Runs before Hibernate validates the schema.
 * <p>
 * Creating the schema needs a role that may create schemas, extensions and roles (e.g. {@code postgres}). In production
 * run {@code run_all.sql} yourself and set {@code lms.schema.auto-create=false}.
 */
@Configuration
public class SchemaInitializer implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);
    private static final String FOLDER = "db/schema/";
    private static final Pattern INCLUDE = Pattern.compile("^\\\\ir\\s+(\\S+)\\s*$", Pattern.MULTILINE);

    private final DataSource dataSource;
    private final boolean enabled;

    public SchemaInitializer(DataSource dataSource, @Value("${lms.schema.auto-create:true}") boolean enabled) {
        this.dataSource = dataSource;
        this.enabled = enabled;
    }

    /** Makes Hibernate (which validates the schema) wait until this has run. */
    @Bean
    public static BeanFactoryPostProcessor schemaBeforeJpa() {
        return beanFactory -> {
            if (!beanFactory.containsBeanDefinition("entityManagerFactory")) return;
            BeanDefinition emf = beanFactory.getBeanDefinition("entityManagerFactory");
            List<String> deps = new ArrayList<>();
            if (emf.getDependsOn() != null) deps.addAll(List.of(emf.getDependsOn()));
            deps.add("schemaInitializer");
            emf.setDependsOn(deps.toArray(String[]::new));
        };
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!enabled) return;
        try (Connection c = dataSource.getConnection()) {
            if (schemaExists(c)) return;
            List<String> files = scriptOrder();
            log.warn("Schema lms_core not found: creating it from {} SQL files in {} (first start)", files.size(), FOLDER);
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                for (String f : files) {
                    try {
                        st.execute(read(FOLDER + f));
                    } catch (SQLException e) {
                        throw new IllegalStateException("Creating the schema failed in " + f + ": " + e.getMessage()
                                + " (nothing was created). Run run_all.sql as postgres, or connect as a role that may create schemas.", e);
                    }
                }
                c.commit();
            } catch (RuntimeException | SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(autoCommit);
            }
            log.warn("Schema lms_core created");
        }
    }

    private static boolean schemaExists(Connection c) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("select to_regclass('lms_core.users') is not null")) {
            return rs.next() && rs.getBoolean(1);
        }
    }

    /** The \ir lines of run_all.sql, in order. */
    private static List<String> scriptOrder() throws IOException {
        Matcher m = INCLUDE.matcher(read(FOLDER + "run_all.sql"));
        List<String> files = new ArrayList<>();
        while (m.find()) files.add(m.group(1));
        if (files.isEmpty()) throw new IllegalStateException(FOLDER + "run_all.sql lists no files");
        return files;
    }

    private static String read(String path) throws IOException {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
