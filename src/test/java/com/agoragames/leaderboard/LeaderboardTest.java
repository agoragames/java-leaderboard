package com.agoragames.leaderboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
		assertEquals("2.0.0", Leaderboard.VERSION);
	}
	
	public void testGetLeaderboardName() {
		assertEquals("name", _leaderboard.getLeaderboardName());
	}
	
	public void testSetPageSize() {
		_leaderboard.setPageSize(10);		
		assertEquals(10, _leaderboard.getPageSize());
		
		_leaderboard.setPageSize(0);
		assertEquals(Leaderboard.DEFAULT_PAGE_SIZE, _leaderboard.getPageSize());
	}
	
	public void testrankMemberAndTotalMembers() {
		rankMembersInLeaderboard(5);
		
		assertEquals(5, _leaderboard.totalMembers());
	}
	
	public void testTotalPages() {
		rankMembersInLeaderboard(Leaderboard.DEFAULT_PAGE_SIZE + 2);
		
		assertEquals(2, _leaderboard.totalPages());
	}
	
	public void testTotalMembersInScoreRange() {
		rankMembersInLeaderboard(5);
		
		assertEquals(3, _leaderboard.totalMembersInScoreRange(2, 4));
	}
	
	public void testScoreFor() {
		_leaderboard.rankMember("member", 76);
		assertEquals(76, (int) _leaderboard.scoreFor("member"));
	}
	
	public void testChangeScoreFor() {
		_leaderboard.rankMember("member", 5);
		assertEquals(5, (int) _leaderboard.scoreFor("member"));
		
		_leaderboard.changeScoreFor("member", 5);
		assertEquals(10, (int) _leaderboard.scoreFor("member"));
	
		_leaderboard.changeScoreFor("member", -5);
		assertEquals(5, (int) _leaderboard.scoreFor("member"));
	}
	
	public void testCheckMember() {
		rankMembersInLeaderboard(5);
		
		assertEquals(true, _leaderboard.checkMember("member_1"));
		assertEquals(false, _leaderboard.checkMember("member_8"));
	}
	
	public void testRankFor() {
		rankMembersInLeaderboard(5);
		
		assertEquals(2, _leaderboard.rankFor("member_4", false));
		assertEquals(1, _leaderboard.rankFor("member_4", true));
	}
	
	public void testRemoveMembersInScoreRange() {
		rankMembersInLeaderboard(5);
		
		assertEquals(5, _leaderboard.totalMembers());
		
		_leaderboard.rankMember("cheater_1", 100);
		_leaderboard.rankMember("cheater_2", 101);
		_leaderboard.rankMember("cheater_3", 102);
		
		assertEquals(8, _leaderboard.totalMembers());
				
	    _leaderboard.removeMembersInScoreRange(100, 102);
		assertEquals(5, _leaderboard.totalMembers());
	}
	
	public void testScoreAndRankFor() {
		rankMembersInLeaderboard(5);
		
		Hashtable<String, Object> data = _leaderboard.scoreAndRankFor("member_1", false);
		
		assertEquals("member_1", data.get("member"));
		assertEquals(1.0, ((Double) data.get("score")).doubleValue());
		assertEquals(5, ((Long) data.get("rank")).longValue());
	}
	
	public void testLeadersIn() {
		rankMembersInLeaderboard(25);
		
		List<LeaderData> leaders = _leaderboard.leadersIn(1, false);
		assertEquals(25, leaders.size());
		assertEquals("member_25", leaders.get(0).getMember());
		assertEquals("member_1", leaders.get(leaders.size() - 1).getMember());
		assertEquals(1, (int) leaders.get(leaders.size() - 1).getScore());
		assertEquals(25, leaders.get(leaders.size() - 1).getRank());
	}
	
	public void testLeadersWithMultiplePages() {
		rankMembersInLeaderboard(Leaderboard.DEFAULT_PAGE_SIZE * 3 + 1);
		
		assertEquals(Leaderboard.DEFAULT_PAGE_SIZE * 3 + 1, _leaderboard.totalMembers());

		List<LeaderData> leaders = _leaderboard.leadersIn(1, false);
		assertEquals(_leaderboard.getPageSize(), leaders.size());

		leaders = _leaderboard.leadersIn(2, false);
		assertEquals(_leaderboard.getPageSize(), leaders.size());

		leaders = _leaderboard.leadersIn(3, false);
		assertEquals(_leaderboard.getPageSize(), leaders.size());

		leaders = _leaderboard.leadersIn(4, false);
		assertEquals(1, leaders.size());
		
		leaders = _leaderboard.leadersIn(_leaderboard.getLeaderboardName(), 1, false, 10);
		assertEquals(10, leaders.size());
	}
	
	public void testAroundMe() {
		rankMembersInLeaderboard(Leaderboard.DEFAULT_PAGE_SIZE * 3 + 1);
		
		assertEquals(Leaderboard.DEFAULT_PAGE_SIZE * 3 + 1, _leaderboard.totalMembers());
		
		List<LeaderData> leadersAroundMe = _leaderboard.aroundMe("member_30", false);
		assertEquals(_leaderboard.getPageSize() / 2, leadersAroundMe.size() / 2);
		
		leadersAroundMe = _leaderboard.aroundMe("member_1", false);
		assertEquals(_leaderboard.getPageSize() / 2 + 1, leadersAroundMe.size());
		
		leadersAroundMe = _leaderboard.aroundMe("member_76", false);
		assertEquals(_leaderboard.getPageSize() / 2, leadersAroundMe.size() / 2);
	}
	
	public void testRankedInList() {
		rankMembersInLeaderboard(Leaderboard.DEFAULT_PAGE_SIZE);
		
		assertEquals(Leaderboard.DEFAULT_PAGE_SIZE, _leaderboard.totalMembers());
		
		List<String> members = new ArrayList<String>();
		members.add("member_1");
		members.add("member_5");
		members.add("member_10");
		
		List<LeaderData> rankedMembers = _leaderboard.rankedInList(members, false);
		assertEquals(3, rankedMembers.size());
		
		assertEquals(25, rankedMembers.get(0).getRank());
		assertEquals(1.0, rankedMembers.get(0).getScore());
	
		assertEquals(21, rankedMembers.get(1).getRank());
		assertEquals(5.0, rankedMembers.get(1).getScore());

		assertEquals(16, rankedMembers.get(2).getRank());
		assertEquals(10.0, rankedMembers.get(2).getScore());
	}
	
	private void rankMembersInLeaderboard(int totalMembers) {
		for (int i = 1; i <= totalMembers; i++) {
			_leaderboard.rankMember("member_" + i, i);
		}
	}
}
