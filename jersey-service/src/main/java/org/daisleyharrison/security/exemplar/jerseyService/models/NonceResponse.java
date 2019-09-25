package org.daisleyharrison.security.samples.jerseyService.models;

public class NonceResponse {
    private String nonce;
    public NonceResponse(String nonce){
        this.nonce = nonce;
    }
    public NonceResponse(){
        
    }
    /**
     * @return String return the nonce
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * @param nonce the nonce to set
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

}