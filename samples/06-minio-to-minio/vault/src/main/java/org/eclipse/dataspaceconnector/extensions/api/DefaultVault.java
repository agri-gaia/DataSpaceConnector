package org.eclipse.dataspaceconnector.extensions.api;

import org.eclipse.dataspaceconnector.spi.result.Result;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implements a vault backed by a properties file.
 */
public class DefaultVault implements Vault {
    private final AtomicReference<Map<String, String>> secrets;

    public DefaultVault() {
        secrets = new AtomicReference<>(new HashMap<>());

        secrets.get().put("username", "admin");
        secrets.get().put("password", "password");
        secrets.get().put("usernamePlatform", "user");
        secrets.get().put("passwordPlatform", "password");
        secrets.get().put("usernameLMIS", "api-test-user");
        secrets.get().put("passwordLMIS", "test1234");
        secrets.get().put("edc.oauth.provider.audience", "https://account.platform.agri-gaia.com/realms/agri-gaia-platform");
        secrets.get().put("edc.oauth.provider.jwks.url", "https://account.platform.agri-gaia.com/realms/agri-gaia-platform/protocol/openid-connect/certs");
        secrets.get().put("edc.oauth.token.url", "https://account.platform.agri-gaia.com/realms/agri-gaia-platform/protocol/openid-connect/token");
        secrets.get().put("edc.oauth.client.id", "ag-test-edc-hsos");
        secrets.get().put("edc.oauth.public.key.alias", "ag-test-edc-hsos");
        secrets.get().put("edc.oauth.private.key.alias", "ag-test-edc-hsos");
    }

    @Override
    public @Nullable
    String resolveSecret(String key) {
        return secrets.get().get(key);
    }

    @Override
    public synchronized Result<Void> storeSecret(String key, String value) {
        var newSecrets = new HashMap<>(secrets.get());
        newSecrets.put(key, value);
        var properties = new Properties();
        properties.putAll(newSecrets);
        secrets.set(newSecrets);
        return Result.success();
    }

    @Override
    public Result<Void> deleteSecret(String key) {
        var newSecrets = new HashMap<>(secrets.get());
        newSecrets.remove(key);
        var properties = new Properties();
        properties.putAll(newSecrets);
        secrets.set(newSecrets);
        return Result.success();
    }
}
