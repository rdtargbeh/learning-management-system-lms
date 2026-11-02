package learning_management_system.backend.utility;

import org.springframework.stereotype.Component;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/** Reads DNS TXT records (used to prove that an institution owns a domain). Returns an empty list when the lookup fails. */
@Component
public class DnsTxtResolver {

    public List<String> txtRecords(String name) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", "2000");
        env.put("com.sun.jndi.dns.timeout.retries", "1");
        List<String> out = new ArrayList<>();
        try {
            Attributes attrs = new InitialDirContext(env).getAttributes("dns:/" + name, new String[]{"TXT"});
            Attribute txt = attrs.get("TXT");
            if (txt == null) return out;
            NamingEnumeration<?> values = txt.getAll();
            while (values.hasMore()) out.add(values.next().toString().replace("\"", "").trim());
        } catch (NamingException | RuntimeException e) {
            // unknown name, no TXT records or no DNS: not verified
        }
        return out;
    }
}
