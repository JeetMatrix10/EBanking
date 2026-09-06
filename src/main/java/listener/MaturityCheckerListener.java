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

		// Why processDueInstallments() no longer takes an argument: test
		// mode is now read PER-RD from the database itself (set once at
		// booking time), not decided globally by this listener. This fixes
		// a real bug where every RD in the project — even ones booked in
		// real mode — was being advanced on 5-minute test timing regardless
		// of what the customer actually chose when booking.
		scheduler.scheduleAtFixedRate(() -> {
			fdDao.processMaturedDeposits();
			rdDao.processDueInstallments();
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