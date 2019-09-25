package org.daisleyharrison.security.common.spi;

import java.util.concurrent.CompletableFuture;

public interface VaultServiceProvider extends SecurityServiceProvider {
    public void setRootPath(String rootPath);
    public void configure() throws Exception;
    public CompletableFuture<String> authenticate(String principleId, char[] password);
    public CompletableFuture<String> createVaultToken(String token, String scope, int uses, int ttl, boolean opaque, boolean renewable);
    public CompletableFuture<String> renewVaultToken(String token, String tokenToRenew);
    public CompletableFuture<String> renewVaultToken(String tokenToRenew);
    public CompletableFuture<Void> revokeVaultToken(String token, String tokenToRevoke);
    public CompletableFuture<Void> revokeVaultToken(String tokenToRevoke);
    public CompletableFuture<String> readFromVault(String token, String path);
    public CompletableFuture<Void> removeFromVault(String token, String path);
    public CompletableFuture<Void> writeToVault(String token, String path, String secret);
}