package Walmart.Round2;

/**
 *  Orderservice
 *
 * @Autowired
 * orderrepo
 *
 * userrepo
 *
 * OrderService(orderrepo)
 *
 * Other case
 * OrderService() {
 * @Mock
 * OrderRepo repo
 *
 * //implement test case
 * }
 *
 * @Transaction(Propagtion = REQUIRED)
 * //REQUIRED -> take forward existing trans or if there no trans exist then it will create new
 *
 * // 100k -- 1M
 * // 6 instances (Invoice-microservice) -  6 pods
 * // Batching (10k) -> @Scheduler() --> sche1 insta 10k --> ssch1 inst 10k -> OLE
 * // version -> OLE (Oracle DB)
 * // Scheduler (LAST_RUN_TIME, IN_PROGRESS)
 * // (Intitial_Delay -> Radom (1 hr)
 * // pick up the record -> kafka -
 * // sched 2  -> (freq -> 1 hr)
 * // kafka -> brokers -> topics -> partition
 * // consumer1 - partion1 ...
 *
 *   3 partion aand 5 consumer
 * // consume 1 --> partion 1, 2,5 -> partion 2 , 3 -> partion 3
 *
 * // 5 pation 3 consumer
 * // 1,3 consumer 1
 *   2
 *   //design caching system
 *
 *   CacheController
 *   Strategy Pattern
 *   CacheController(Cache)
 *   CachePolicy(int k, int v) -> strtegy pattermm -> get and put
 */

/**
 *  interface CachePolicy<K> {keyAccess(k), evict()}
 *  Cache<K,V> {capacity, Map<> storage, CachePolicy}
 */

//class ABC {
//
//    void controller() {
//
//    }
//
//    @Transactioal(Propagtion = REQUIRED)
//    void fn1() {
//        //op1; --> save()
//        //op2
//        fn2();
//    }
//
//    @Transactioal(Propagtion = REQUIRED))
//    void fn2() {
//        //op1
//        //op2
//    }
//}
public class Main {
}
