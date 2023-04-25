package org.eclipse.dataspaceconnector.extensions.api;

import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.BaseExtension;
import org.eclipse.dataspaceconnector.spi.system.Provides;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

/**
 * Bootstraps the file system-based vault extension.
 */
@BaseExtension
@Provides({ Vault.class})
public class DefaultVaultExtension implements ServiceExtension {

    @Override
    public String name() {
        return "Default Vault";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var vault = initializeVault();
        context.registerService(Vault.class, vault);
    }

    private Vault initializeVault() {
        return new DefaultVault();
    }
}
