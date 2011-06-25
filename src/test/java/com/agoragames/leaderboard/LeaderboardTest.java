package com.agoragames.leaderboard;

import junit.framework.TestCase;
import redis.clients.jedis.Jedis;

public class LeaderboardTest extends TestCase {

	private Jedis _jedis;
	private Leaderboard _leaderboard;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		_leaderboard = new Leaderboard("name");
		_jedis = new Jedis(Leaderboard.DEFAULT_REDIS_HOST, Leaderboard.DEFAULT_REDIS_PORT);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		_jedis.flushDB();
		_leaderboard.disconnect();
		_jedis.disconnect();
	}

	public void testVersion() {
		assertEquals("1.0.0", Leaderboard.VERSION);
	}
	
	public void testSetPageSize() {
		_leaderboard.setPageSize(10);		
		assertEquals(10, _leaderboard.getPageSize());
		
		_leaderboard.setPageSize(0);
		assertEquals(Leaderboard.DEFAULT_PAGE_SIZE, _leaderboard.getPageSize());
	}
	
	public void testAddMemberAndTotalMembers() {
		addMembersToLeaderboard(5);
		
		assertEquals(5, _leaderboard.totalMembers());
	}
	
	public void testTotalPages() {
		addMembersToLeaderboard(Leaderboard.DEFAULT_PAGE_SIZE + 2);
		
		assertEquals(2, _leaderboard.totalPages());
	}
	
	private void addMembersToLeaderboard(int totalMembers) {
		for (int i = 0; i < totalMembers; i++) {
			_leaderboard.addMember("member_" + i, i);
		}
	}
}
