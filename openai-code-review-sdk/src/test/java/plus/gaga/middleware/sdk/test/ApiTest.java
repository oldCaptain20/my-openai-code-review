package plus.gaga.middleware.sdk.test;


import plus.gaga.middleware.sdk.type.utils.BearerTokenUtils;

public class ApiTest {

    public static void main(String[] args) {
        String apiKeySecret = "a1a614d1edbd471b84536b4626e4615b.DofbLZQYkFOfrBtM";
        String token = BearerTokenUtils.getToken(apiKeySecret);
        System.out.println(token);
    }





}
