package model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {

    private int tokenId;

    @JsonCreator
    public Token(@JsonProperty("tokenId") int tokenId) {
        this.tokenId = tokenId;
    }

    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }


}
