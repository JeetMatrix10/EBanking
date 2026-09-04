package listener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import dao.FixedDepositDao;

// Why @WebListener instead of registering this in web.xml: same reasoning
// as @WebServlet earlier — annotation-based registration keeps everything
// in one file instead of split across Java code and XML config.
@WebListener
public class MaturityCheckerListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    // Why contextInitialized: this method runs ONCE, automatically, the
    // moment Tomcat finishes starting your app — exactly where "start a
    // background job that runs for as long as the server is up" belongs.
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        FixedDepositDao dao = new FixedDepositDao();

        // Why scheduleAtFixedRate with a 30-second interval: frequent enough
        // that in test mode (minutes-based maturity) you'll see it credit
        // within half a minute of watching, but not so frequent that it
        // hammers the database pointlessly when running for real (years-based).
        scheduler.scheduleAtFixedRate(() -> {
            dao.processMaturedDeposits();
        }, 0, 30, TimeUnit.SECONDS);

        System.out.println("FD Maturity Checker started — running every 30 seconds.");
    }

    // Why contextDestroyed matters: without explicitly shutting down the
    // scheduler when the app stops, the background thread could keep
    // running or leak resources even after you redeploy/restart in Eclipse.
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        System.out.println("FD Maturity Checker stopped.");
    }
}