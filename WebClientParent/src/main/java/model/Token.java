package model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {

    private int tokenId;

    private int status;

    @JsonCreator
    public Token(@JsonProperty("tokenId") int tokenId,@JsonProperty("status") int status) {
        this.tokenId = tokenId;
        this.status = status;
    }



    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


}
