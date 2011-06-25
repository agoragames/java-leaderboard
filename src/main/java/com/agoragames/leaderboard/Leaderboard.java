package com.agoragames.leaderboard;

import java.util.Hashtable;

import redis.clients.jedis.Jedis;

public class Leaderboard {
	
	public static final String VERSION = "1.0.0";
	public static final int DEFAULT_PAGE_SIZE = 25;
	public static final String DEFAULT_REDIS_HOST = "localhost";
	public static final int DEFAULT_REDIS_PORT = 6379;

	private Jedis _jedis;
	private String _leaderboardName;
	private int _pageSize;
		
	public Leaderboard(String leaderboardName) {
		this(leaderboardName, DEFAULT_REDIS_HOST, DEFAULT_REDIS_PORT, DEFAULT_PAGE_SIZE);
	}
	
	public Leaderboard(String leaderboardName, String host, int port, int pageSize) {
		_leaderboardName = leaderboardName;
		_pageSize = pageSize;
		
		if (_pageSize < 1) {
			_pageSize = DEFAULT_PAGE_SIZE;
		}
		
		_jedis = new Jedis(host, port);
	}
	
	public int getPageSize() {
		return _pageSize;
	}
	
	public void setPageSize(int pageSize) {
		if (pageSize < 1) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		
		_pageSize = pageSize;
	}
	
	public void disconnect() {
		_jedis.disconnect();
	}
	
	public long totalMembers() {
		return this.totalMembersIn(_leaderboardName);
	}
	
	public long totalMembersIn(String leaderboardName) {
		return _jedis.zcard(leaderboardName);
	}
		
	public int totalPages() {
		return totalPagesIn(_leaderboardName, null);
	}
	
	public int totalPagesIn(String leaderboardName, Integer pageSize) {
		if (pageSize == null) {
			pageSize = _pageSize;
		}
		
		return (int) Math.ceil((float) totalMembersIn(_leaderboardName) / (float) pageSize);		
	}
	
	public long totalMembersInScoreRange(double minScore, double maxScore) {
		return totalMembersInScoreRangeIn(_leaderboardName, minScore, maxScore);
	}
	
	public long totalMembersInScoreRangeIn(String leaderboardName, double minScore, double maxScore) {
		return _jedis.zcount(leaderboardName, minScore, maxScore);
	}

	public long addMember(String member, double score) {
		return this.addMemberTo(_leaderboardName, member, score);
	}

	public long addMemberTo(String leaderboardName, String member, double score) {
		return _jedis.zadd(leaderboardName, score, member);
	}
	
	public double scoreFor(String member) {
		return scoreForIn(_leaderboardName, member);
	}
	
	public double scoreForIn(String leaderboardName, String member) {
		return _jedis.zscore(leaderboardName, member);
	}
	
	public double changeScoreFor(String member, double delta) {
		return changeScoreForMemberIn(_leaderboardName, member, delta);
	}
	
	public double changeScoreForMemberIn(String leaderboardName, String member, double delta) {
		return _jedis.zincrby(_leaderboardName, delta, member); 
	}
	
	public boolean checkMember(String member) {
		return checkMemberIn(_leaderboardName, member);
	}
	
	public boolean checkMemberIn(String leaderboardName, String member) {
		return !(_jedis.zscore(leaderboardName, member) == null);
	}
	
	public long rankFor(String member, boolean useZeroIndexForRank) {
		return rankForIn(_leaderboardName, member, useZeroIndexForRank);
	}
	
	public long rankForIn(String leaderboardName, String member, boolean useZeroIndexForRank) {
		if (useZeroIndexForRank) {
			return _jedis.zrevrank(leaderboardName, member);
		} else {
			return (_jedis.zrevrank(leaderboardName, member) + 1);
		}		
	}
	
	public long removeMembersInScoreRange(double minScore, double maxScore) {
		return removeMembersInScoreRangeIn(_leaderboardName, minScore, maxScore);
	}
	
	public long removeMembersInScoreRangeIn(String leaderboardName, double minScore, double maxScore) {
		return _jedis.zremrangeByScore(leaderboardName, minScore, maxScore);
	}
	
	public Hashtable<String, Object> scoreAndRankFor(String member, boolean useZeroIndexForRank) {
		return scoreAndRankForIn(_leaderboardName, member, useZeroIndexForRank);
	}
	
	public Hashtable<String, Object> scoreAndRankForIn(String leaderboardName, String member, boolean useZeroIndexForRank) {
		Hashtable<String, Object> data = new Hashtable<String, Object>();
		
		data.put("member", member);
		data.put("score", scoreForIn(_leaderboardName, member));
		data.put("rank", rankForIn(_leaderboardName, member, useZeroIndexForRank));
		
		return data;
	}
}