class TestThread3a extends Thread {

    // limit to calculate fibonacci up to
    private int LIMIT = 50;

    public TestThread3a () {}

    // TestThread3a exists to deliberately eat up a bunch of cpu time.
    // It accomplishes this by calculating the fibonacci sequence of a fixed limit.
    public void run( ) {
    	long fib = fibonacci(LIMIT);
        SysLib.cout("fib: " + fib + "\n");
		SysLib.exit( );
    }

    private long fibonacci(int n) {
        if (n <= 1) {
            return n;
        }
        else {
            return fibonacci(n - 1) + fibonacci(n - 2);
        }
    }
}

