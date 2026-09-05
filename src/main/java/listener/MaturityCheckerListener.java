package listener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import dao.FixedDepositDao;
import dao.RecurringDepositDao;

@WebListener
public class MaturityCheckerListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Why both DAOs are declared here, BEFORE the scheduler block below:
        // Java requires a variable to exist before any code can reference it —
        // declaring both up front also makes it clear at a glance that this
        // one background job handles two separate responsibilities (FD and RD).
        FixedDepositDao fdDao = new FixedDepositDao();
        RecurringDepositDao rdDao = new RecurringDepositDao();

        // Why ONE scheduleAtFixedRate call handling both, not two separate
        // calls: this is a single background job that checks two things
        // every cycle — running two independent schedulers would mean two
        // separate threads doing overlapping, uncoordinated work for no benefit.
        scheduler.scheduleAtFixedRate(() -> {
            fdDao.processMaturedDeposits();
            rdDao.processDueInstallments(true); // true = test mode timing active project-wide
        }, 0, 30, TimeUnit.SECONDS);

        System.out.println("FD/RD Maturity Checker started — running every 30 seconds.");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        System.out.println("FD/RD Maturity Checker stopped.");
    }
}