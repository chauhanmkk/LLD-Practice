////design pattern - impleentation (working code)
////Rate limitor - Token bucket
////mp <user, tokenbucket> -- find out if user allowed or not
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class RateLimitor {
//    class TokenBuckett {
//        int capicity;
//        double refillRate;
//        Long lastRefillTime;
//        double currentToken;
//
//        TokenBuckett(int capicity, double refillRate) {
//            this.currentToken = 10;
//            this.capicity = capicity;
//            this.refillRate = refillRate;
//            this.lastRefillTime = System.currentTimeMillis();
//        }
//    }
//
//    Map<String, TokenBucket> map = new ConcurrentHashMap<>();
//    RateLimitor() {
//        map.put("mohit", new TokenBucket(10,60000));
//    }
//
//    boolean isAllowed(String user) {
//        double tokens = refill(user, map.getOrDefault(user, new TokenBucket(10,60000)));
//        if(tokens >= 1) {
//            TokenBucket bucket = map.get(user);
//            bucket.currentToken--;
//            map.put(user, bucket);
//            System.out.println("Ok");
//            return true;
//        }
//        System.out.println("429 too many requests");
//        return false;
//    }
//
//    double refill(String user, TokenBucket bucket) {
//        long elapseTime = System.currentTimeMillis() - bucket.lastRefillTime;
//        double tokens = (elapseTime * bucket.refillRate);
//        bucket.lastRefillTime = System.currentTimeMillis();
//        bucket.currentToken = Math.min(bucket.capicity, bucket.currentToken + tokens);
////        map.put(user, bucket);
//        return bucket.currentToken;
//    }
//
//    static void main() {
//        String user = "mohit";
//        RateLimitor rateLimitor = new RateLimitor();
//        for(int i=0;i<50;i++) {
//            rateLimitor.isAllowed(user);
//        }
//    }
//}
