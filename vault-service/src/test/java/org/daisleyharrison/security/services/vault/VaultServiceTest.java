package org.daisleyharrison.security.services.vault;

import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.io.File;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.spi.VaultServiceProvider;
import org.daisleyharrison.security.services.vault.models.Policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Ignore
public class VaultServiceTest {
    public static final String TEST_VAULT_ROOT_PATH = "./vault-service/data/test-vault";
    public static final String TEST_VAULT_ROOT_PATH_COMPONENT = "vault-service";

    private String locatePath(String path, String component) {
        if (currentDirectory().contains(component)) {
            return "." + path;
        } else {
            return path;
        }
    }

    private String currentDirectory() {
        return new File(".").getAbsolutePath();
    }

    VaultServiceProvider getVaultService() {
        ServiceLoader<VaultServiceProvider> loader = ServiceLoader.load(VaultServiceProvider.class);
        Optional<VaultServiceProvider> vaultServiceProvider = loader.findFirst();
        assertNotNull(vaultServiceProvider);
        return vaultServiceProvider.get();
    }

    VaultServiceProvider getConfiguredVaultService() throws Exception {
        VaultServiceProvider vaultService = getVaultService();
        assertFalse(vaultService == null);
        if (!vaultService.isInitialized()) {
            try (Stage stage = vaultService.beginInitialize()) {
                String rootPath = locatePath(TEST_VAULT_ROOT_PATH, TEST_VAULT_ROOT_PATH_COMPONENT);
                vaultService.setRootPath(rootPath);
                vaultService.configure();
            }
        }
        return vaultService;
    }

    /**
     * Test to see that the VaultServiceProvider can be instantiated.
     */
    @Test
    public void testServiceInstantiation() throws Exception {
        VaultServiceProvider vaultService = getVaultService();
        assertNotNull(vaultService);
    }

    @Test
    public void testConfigure() throws Exception {
        VaultServiceProvider vaultService = getConfiguredVaultService();
        assertNotNull(vaultService);
    }

    @Test
    public void testPolicy() throws Exception {
        Policy policy = Policy.ALLOW_ALL;
        assertNotNull(policy.toString());
    }

    public static final String QUICK = "The quick brown fox jumped over the lazy dog.";

    @Test(expected = ExecutionException.class)
    public void testReadInvalidPathQuick() throws Exception {
        VaultServiceProvider vaultService = getConfiguredVaultService();
        assertNotNull(vaultService);
        String token = vaultService.authenticate("root", "password".toCharArray()).get();
        String secret = vaultService.readFromVault(token, "this/is/a/path/that/does/not/exist").get();
        assertNull(secret);
        vaultService.revokeVaultToken(token).get();
    }

    @Test
    public void testRevokeOpaqueToken() throws Exception {
        VaultServiceProvider vaultService = getConfiguredVaultService();
        assertNotNull(vaultService);
        String token = vaultService.authenticate("root", "password".toCharArray()).get();
        vaultService.revokeVaultToken(token).get();
        try {
            vaultService.writeToVault(token, "test/unittest/quick", QUICK).get();
            assertTrue("Failed to revoke token", false);
        }
        catch(ExecutionException exception){

        }
    }

    @Test
    public void testRevokeJwtToken() throws Exception {
        VaultServiceProvider vaultService = getConfiguredVaultService();
        assertNotNull(vaultService);
        String token = vaultService.authenticate("root", "password".toCharArray()).get();
        String jwtToken = vaultService.createVaultToken(token, "test/unittest/quick", 0, 20, true, false).get();
        vaultService.revokeVaultToken(token, jwtToken).get();
        try {
            vaultService.writeToVault(jwtToken, "test/unittest/quick", QUICK).get();
            assertTrue("Failed to revoke token", false);
        }
        catch(ExecutionException exception){

        }
    }

    @Test
    public void testWriteReadQuick() throws Exception {
        VaultServiceProvider vaultService = getConfiguredVaultService();
        assertNotNull(vaultService);
        String token = vaultService.authenticate("root", "password".toCharArray()).get();
        vaultService.writeToVault(token, "test/unittest/quick", QUICK).get();
        String quick = vaultService.readFromVault(token, "test/unittest/quick").get();
        vaultService.revokeVaultToken(token).get();
        assertEquals(quick, QUICK);
    }

    @Test
    public void testWriteReadQuickOpaqueToken() throws Exception {
        VaultServiceProvider vaultService = getConfiguredVaultService();
        assertNotNull(vaultService);
        String token = vaultService.authenticate("root", "password".toCharArray()).get();
        String opaqueToken = vaultService.createVaultToken(token, "test/unittest/quick", 3, 20, true, false).get();
        vaultService.writeToVault(opaqueToken, "test/unittest/quick", QUICK).get();
        String quick = vaultService.readFromVault(opaqueToken, "test/unittest/quick").get();
        assertEquals(quick, QUICK);
        vaultService.revokeVaultToken(token, opaqueToken).get();
        vaultService.revokeVaultToken(token).get();
    }

    @Test
    public void testWriteReadQuickJwtToken() throws Exception {
        VaultServiceProvider vaultService = getConfiguredVaultService();
        assertNotNull(vaultService);
        String token = vaultService.authenticate("root", "password".toCharArray()).get();
        String jwtToken = vaultService.createVaultToken(token, "test/unittest/quick", 0, 20, false, false).get();
        vaultService.writeToVault(jwtToken, "test/unittest/quick", QUICK).get();
        String quick = vaultService.readFromVault(jwtToken, "test/unittest/quick").get();
        assertEquals(quick, QUICK);
        vaultService.revokeVaultToken(token, jwtToken).get();
        vaultService.revokeVaultToken(token).get();
    }
}
