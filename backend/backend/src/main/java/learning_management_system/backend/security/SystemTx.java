package learning_management_system.backend.security;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Runs trusted server-side work (sign-in lookups, bootstrap, support-access bookkeeping) in its own
 * transaction with a system database context. Never call this with input that bypasses an authorization check.
 */
@Component
public class SystemTx {
    private final TransactionTemplate tx;

    public SystemTx(PlatformTransactionManager tm) {
        this.tx = new TransactionTemplate(tm);
    }

    public <T> T platform(Supplier<T> work) {
        return RequestContext.runAs(RequestContext.system(), () -> tx.execute(s -> work.get()));
    }

    public <T> T inTenant(UUID tenantId, Supplier<T> work) {
        return RequestContext.runAs(RequestContext.systemInTenant(tenantId), () -> tx.execute(s -> work.get()));
    }
}
