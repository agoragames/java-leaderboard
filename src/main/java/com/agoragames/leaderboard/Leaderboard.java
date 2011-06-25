package com.agoragames.leaderboard;

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

	public long addMember(String member, double score) {
		return this.addMemberTo(_leaderboardName, member, score);
	}

	public long addMemberTo(String leaderboardName, String member, double score) {
		return _jedis.zadd(leaderboardName, score, member);
	}
	
	
}