package land.chipmunk.chipmunkmod.modules;

public class TransactionManager {
    public static final TransactionManager INSTANCE = new TransactionManager();

    private int transactionId = 0;

    public int transactionId () { return transactionId; }

    public int nextTransactionId () { return transactionId++; }
}
