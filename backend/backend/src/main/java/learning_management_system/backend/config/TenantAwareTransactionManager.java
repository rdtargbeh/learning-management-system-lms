package learning_management_system.backend.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import learning_management_system.backend.security.RequestContext;
import org.hibernate.Session;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.PreparedStatement;

/**
 * JPA transaction manager that, at the start of every transaction, sets the PostgreSQL session
 * variables used by row-level security. SET LOCAL semantics (is_local = true) mean the values
 * vanish at commit/rollback, so pooled connections never leak a tenant to the next request.
 */
public class TenantAwareTransactionManager extends JpaTransactionManager {

    private static final String SQL =
            "select set_config('app.current_tenant_id', ?, true), " +
            "set_config('app.current_user_id', ?, true), " +
            "set_config('app.is_platform_admin', ?, true)";

    public TenantAwareTransactionManager(EntityManagerFactory emf) {
        super(emf);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);
        EntityManagerHolder holder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(obtainEntityManagerFactory());
        if (holder == null) return;
        EntityManager em = holder.getEntityManager();
        RequestContext ctx = RequestContext.current();
        em.unwrap(Session.class).doWork(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SQL)) {
                ps.setString(1, ctx.tenantId() == null ? "" : ctx.tenantId().toString());
                ps.setString(2, ctx.userId() == null ? "" : ctx.userId().toString());
                ps.setString(3, ctx.platformAdmin() ? "on" : "off");
                ps.execute();
            }
        });
    }
}
