package com.mycorp;

import java.io.Closeable;

import com.ning.http.client.AsyncHttpClient;

public class Builder implements Closeable {
  
	private AsyncHttpClient client;
    private String url;
    private String username;
    private String password;
    private String token;
    private String oauthToken;

    public Builder(String url) {
        this.url = url;
    }
    
    public Builder(String user, String token) {
        this.username = user;
        this.token = token;        
    }
    

	public AsyncHttpClient getClient() {
		return client;
	}

	public void setClient(AsyncHttpClient client) {
		this.client = client;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getOauthToken() {
		return oauthToken;
	}

	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url=url;
	}

   /* public Builder setUsername(String username) {
        this.username = username;
        return this;
    }

    public Builder setPassword(String password) {
        this.password = password;
        if (password != null) {
            this.token = null;
            this.oauthToken = null;
        }
        return this;
    }

    public Builder setToken(String token) {
        this.token = token;
        if (token != null) {
            this.password = null;
            this.oauthToken = null;
        }
        return this;
    }*/

   public Zendesk build() {
            return new Zendesk(this);

   }
    
   public boolean isClosed() {
       return client.isClosed();
   }

   public void close() {
       if (!client.isClosed()) {
           client.close();
       }
   }
   
   
   
   
}
