package com.agoragames.leaderboard;

import java.util.Hashtable;

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
	
	public void testTotalMembersInScoreRange() {
		addMembersToLeaderboard(5);
		
		assertEquals(3, _leaderboard.totalMembersInScoreRange(2, 4));
	}
	
	public void testScoreFor() {
		_leaderboard.addMember("member", 76);
		assertEquals(76, (int) _leaderboard.scoreFor("member"));
	}
	
	public void testChangeScoreFor() {
		_leaderboard.addMember("member", 5);
		assertEquals(5, (int) _leaderboard.scoreFor("member"));
		
		_leaderboard.changeScoreFor("member", 5);
		assertEquals(10, (int) _leaderboard.scoreFor("member"));
	
		_leaderboard.changeScoreFor("member", -5);
		assertEquals(5, (int) _leaderboard.scoreFor("member"));
	}
	
	public void testCheckMember() {
		addMembersToLeaderboard(5);
		
		assertEquals(true, _leaderboard.checkMember("member_1"));
		assertEquals(false, _leaderboard.checkMember("member_8"));
	}
	
	public void testRankFor() {
		addMembersToLeaderboard(5);
		
		assertEquals(2, _leaderboard.rankFor("member_4", false));
		assertEquals(1, _leaderboard.rankFor("member_4", true));
	}
	
	public void testRemoveMembersInScoreRange() {
		addMembersToLeaderboard(5);
		
		assertEquals(5, _leaderboard.totalMembers());
		
		_leaderboard.addMember("cheater_1", 100);
		_leaderboard.addMember("cheater_2", 101);
		_leaderboard.addMember("cheater_3", 102);
		
		assertEquals(8, _leaderboard.totalMembers());
				
	    _leaderboard.removeMembersInScoreRange(100, 102);
		assertEquals(5, _leaderboard.totalMembers());
	}
	
	public void testScoreAndRankFor() {
		addMembersToLeaderboard(5);
		
		Hashtable<String, Object> data = _leaderboard.scoreAndRankFor("member_1", false);
		
		assertEquals("member_1", data.get("member"));
		assertEquals(1.0, ((Double) data.get("score")).doubleValue());
		assertEquals(5, ((Long) data.get("rank")).longValue());
	}
	
	private void addMembersToLeaderboard(int totalMembers) {
		for (int i = 1; i <= totalMembers; i++) {
			_leaderboard.addMember("member_" + i, i);
		}
	}
}
