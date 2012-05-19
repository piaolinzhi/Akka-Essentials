package org.akka.essentials.stm.transactor.example2;

import static akka.pattern.Patterns.ask;

import org.junit.Assert;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.Await;
import akka.testkit.TestKit;
import akka.util.Duration;

public class BankTest extends TestKit {
	static ActorSystem _system = ActorSystem.create("STM-Example");

	public BankTest() {
		super(_system);
	}

	@Test
	public void successTest() throws Exception {
		ActorRef transfer = _system.actorOf(new Props(TransferActor.class));
		transfer.tell(new TransferMsg(Float.valueOf("1777")));

		AccountBalance balance = (AccountBalance) Await.result(
				ask(transfer, new AccountBalance("XYZ"), 5000),
				Duration.parse("5 second"));

		Assert.assertEquals(Float.parseFloat("3223"), balance.getBalance(),
				Float.parseFloat("0"));

		balance = (AccountBalance) Await.result(
				ask(transfer, new AccountBalance("ABC"), 5000),
				Duration.parse("5 second"));

		Assert.assertEquals(Float.parseFloat("2777"), balance.getBalance(),
				Float.parseFloat("0"));

	}

	@Test
	public void failureTest() throws Exception {
		ActorRef transfer = _system.actorOf(new Props(TransferActor.class));

		transfer.tell(new TransferMsg(Float.valueOf("5500")));

		// sleeping to allow some time for actors to be restarted
		Thread.sleep(4000);

		AccountBalance balance = (AccountBalance) Await.result(
				ask(transfer, new AccountBalance("XYZ"), 5000),
				Duration.parse("5 second"));

		Assert.assertEquals(Float.parseFloat("5000"), balance.getBalance(),
				Float.parseFloat("0"));

		balance = (AccountBalance) Await.result(
				ask(transfer, new AccountBalance("ABC"), 5000),
				Duration.parse("5 second"));

		Assert.assertEquals(Float.parseFloat("1000"), balance.getBalance(),
				Float.parseFloat("0"));

	}

	@Test
	public void accountTest() throws Exception {
		ActorRef transfer = _system.actorOf(new Props(TransferActor.class));

		transfer.tell(new AccountDebit(Float.parseFloat("1000")));
		transfer.tell(new AccountCredit(Float.parseFloat("1000")));
		transfer.tell(new AccountDebit(Float.parseFloat("1000")));
		transfer.tell(new AccountDebit(Float.parseFloat("1000")));
		transfer.tell(new AccountDebit(Float.parseFloat("3500")));
		transfer.tell(new AccountCredit(Float.parseFloat("2500")));
		transfer.tell(new AccountDebit(Float.parseFloat("3500")));

		// sleeping to allow some time for all messages to be processed
		Thread.sleep(4000);

		AccountBalance balance = (AccountBalance) Await.result(
				ask(transfer, new AccountBalance("XYZ"), 5000),
				Duration.parse("5 second"));

		Assert.assertEquals(Float.parseFloat("2000"), balance.getBalance(),
				Float.parseFloat("0"));

	}

}
