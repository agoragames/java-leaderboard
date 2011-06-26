package com.agoragames.leaderboard;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

public class Leaderboard {
	
	public static final String VERSION = "1.0.0";
	public static final int DEFAULT_PAGE_SIZE = 25;
	public static final String DEFAULT_REDIS_HOST = "localhost";
	public static final int DEFAULT_REDIS_PORT = 6379;

	private Jedis _jedis;
	private String _leaderboardName;
	private int _pageSize;
	
	/**
	 * Create a leaderboard using the default host, default port, and default page size
	 * 
	 * @param leaderboardName Name of the leaderboard
	 */
	public Leaderboard(String leaderboardName) {
		this(leaderboardName, DEFAULT_REDIS_HOST, DEFAULT_REDIS_PORT, DEFAULT_PAGE_SIZE);
	}
	
	/**
	 * Create a leaderboard with a given name, host, port and page size
	 * 
	 * @param leaderboardName Name of the leaderboard
	 * @param host Redis host
	 * @param port Redis port
	 * @param pageSize Page size
	 */
	public Leaderboard(String leaderboardName, String host, int port, int pageSize) {
		_leaderboardName = leaderboardName;
		_pageSize = pageSize;
		
		if (_pageSize < 1) {
			_pageSize = DEFAULT_PAGE_SIZE;
		}
		
		_jedis = new Jedis(host, port);
	}
	
	/**
	 * Get the leaderboard name
	 * 
	 * @return Leaderboard name
	 */
	public String getLeaderboardName() {
		return _leaderboardName;		
	}
	
	/**
	 * Get the page size
	 * 
	 * @return Page size
	 */
	public int getPageSize() {
		return _pageSize;
	}
	
	/**
	 * Set the page size
	 * 
	 * @param pageSize Page size
	 */
	public void setPageSize(int pageSize) {
		if (pageSize < 1) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		
		_pageSize = pageSize;
	}
	
	/**
	 * Disconnect from the Redis instance
	 */
	public void disconnect() {
		_jedis.disconnect();
	}
	
	/**
	 * Return the total # of members in the current leaderboard
	 * 
	 * @return Total # of members in the current leaderboard
	 */
	public long totalMembers() {
		return this.totalMembersIn(_leaderboardName);
	}
	
	/**
	 * Return the total # of members in the named leaderboard
	 * 
	 * @param leaderboardName Leaderboard
	 * @return Total # of members in the leaderboard
	 */
	public long totalMembersIn(String leaderboardName) {
		return _jedis.zcard(leaderboardName);
	}
	
	/**
	 * Return the total # of pages in the current leaderboard
	 * 
	 * @return Total # of pages in the current leaderboard
	 */
	public int totalPages() {
		return totalPagesIn(_leaderboardName, null);
	}
	
	/**
	 * Return the total # of pages in the named leaderboard
	 * 
	 * @param leaderboardName Leaderboard
	 * @param pageSize Page size
	 * @return Total # of pages in the named leaderboard
	 */
	public int totalPagesIn(String leaderboardName, Integer pageSize) {
		if (pageSize == null) {
			pageSize = _pageSize;
		}
		
		return (int) Math.ceil((float) totalMembersIn(_leaderboardName) / (float) pageSize);		
	}
	
	/**
	 * Return the total # of members in the current leaderboard in a score range
	 *  
	 * @param minScore Minimum score
	 * @param maxScore Maximum score
	 * @return Total # of members in the current leaderboard in a score range
	 */
	public long totalMembersInScoreRange(double minScore, double maxScore) {
		return totalMembersInScoreRangeIn(_leaderboardName, minScore, maxScore);
	}
	
	/**
	 * Return the total # of members in the named leaderboard in a score range
	 *  
	 * @param leaderboardName Leaderboard
	 * @param minScore Minimum score
	 * @param maxScore Maximum score
	 * @return Total # of members in the named leaderboard in a score range
	 */
	public long totalMembersInScoreRangeIn(String leaderboardName, double minScore, double maxScore) {
		return _jedis.zcount(leaderboardName, minScore, maxScore);
	}

	/**
	 * 
	 * @param member
	 * @param score
	 * @return
	 */
	public long addMember(String member, double score) {
		return this.addMemberTo(_leaderboardName, member, score);
	}

	/**
	 * 
	 * @param leaderboardName
	 * @param member
	 * @param score
	 * @return
	 */
	public long addMemberTo(String leaderboardName, String member, double score) {
		return _jedis.zadd(leaderboardName, score, member);
	}
	
	/**
	 * 
	 * @param member
	 * @return
	 */
	public double scoreFor(String member) {
		return scoreForIn(_leaderboardName, member);
	}
	
	/**
	 * 
	 * @param leaderboardName
	 * @param member
	 * @return
	 */
	public double scoreForIn(String leaderboardName, String member) {
		return _jedis.zscore(leaderboardName, member);
	}
	
	/**
	 * 
	 * @param member
	 * @param delta
	 * @return
	 */
	public double changeScoreFor(String member, double delta) {
		return changeScoreForMemberIn(_leaderboardName, member, delta);
	}
	
	/**
	 * 
	 * @param leaderboardName
	 * @param member
	 * @param delta
	 * @return
	 */
	public double changeScoreForMemberIn(String leaderboardName, String member, double delta) {
		return _jedis.zincrby(_leaderboardName, delta, member); 
	}
	
	/**
	 * 
	 * @param member
	 * @return
	 */
	public boolean checkMember(String member) {
		return checkMemberIn(_leaderboardName, member);
	}
	
	/**
	 * 
	 * @param leaderboardName
	 * @param member
	 * @return
	 */
	public boolean checkMemberIn(String leaderboardName, String member) {
		return !(_jedis.zscore(leaderboardName, member) == null);
	}
	
	/**
	 * 
	 * @param member
	 * @param useZeroIndexForRank
	 * @return
	 */
	public long rankFor(String member, boolean useZeroIndexForRank) {
		return rankForIn(_leaderboardName, member, useZeroIndexForRank);
	}
	
	/**
	 * 
	 * @param leaderboardName
	 * @param member
	 * @param useZeroIndexForRank
	 * @return
	 */
	public long rankForIn(String leaderboardName, String member, boolean useZeroIndexForRank) {
		if (useZeroIndexForRank) {
			return _jedis.zrevrank(leaderboardName, member);
		} else {
			return (_jedis.zrevrank(leaderboardName, member) + 1);
		}		
	}
	
	/**
	 * 
	 * @param minScore
	 * @param maxScore
	 * @return
	 */
	public long removeMembersInScoreRange(double minScore, double maxScore) {
		return removeMembersInScoreRangeIn(_leaderboardName, minScore, maxScore);
	}
	
	/**
	 * 
	 * @param leaderboardName
	 * @param minScore
	 * @param maxScore
	 * @return
	 */
	public long removeMembersInScoreRangeIn(String leaderboardName, double minScore, double maxScore) {
		return _jedis.zremrangeByScore(leaderboardName, minScore, maxScore);
	}
	
	/**
	 * 
	 * @param member
	 * @param useZeroIndexForRank
	 * @return
	 */
	public Hashtable<String, Object> scoreAndRankFor(String member, boolean useZeroIndexForRank) {
		return scoreAndRankForIn(_leaderboardName, member, useZeroIndexForRank);
	}
	
	/**
	 * 
	 * @param leaderboardName
	 * @param member
	 * @param useZeroIndexForRank
	 * @return
	 */
	public Hashtable<String, Object> scoreAndRankForIn(String leaderboardName, String member, boolean useZeroIndexForRank) {
		Hashtable<String, Object> data = new Hashtable<String, Object>();
		
		data.put("member", member);
		data.put("score", scoreForIn(_leaderboardName, member));
		data.put("rank", rankForIn(_leaderboardName, member, useZeroIndexForRank));
		
		return data;
	}
	
	/**
	 * 
	 * @param currentPage
	 * @param useZeroIndexForRank
	 * @return
	 */
	public List<LeaderData> leadersIn(int currentPage, boolean useZeroIndexForRank) {
		return leadersIn(_leaderboardName, currentPage, useZeroIndexForRank, _pageSize);
	}
	
	/**
	 * 
	 * @param leaderboardName
	 * @param currentPage
	 * @param useZeroIndexForRank
	 * @param pageSize
	 * @return
	 */
	public List<LeaderData> leadersIn(String leaderboardName, int currentPage, boolean useZeroIndexForRank, int pageSize) {
		if (currentPage < 1) {
			currentPage = 1;
		}
		
		if (pageSize < 1) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		
		if (currentPage > totalPagesIn(leaderboardName, pageSize)) {
			currentPage = totalPagesIn(leaderboardName, pageSize);
		}
		
		int indexForRedis = currentPage - 1;
		int startingOffset = indexForRedis * pageSize;
		if (startingOffset < 0) {
			startingOffset = 0;
		}
		int endingOffset = (startingOffset + pageSize) - 1;
				
		Set<Tuple> rawLeaderData = _jedis.zrevrangeWithScores(leaderboardName, startingOffset, endingOffset);
		return massageLeaderData(leaderboardName, rawLeaderData, useZeroIndexForRank);	
	}
	
	/**
	 * 
	 * @param member
	 * @param useZeroIndexForRank
	 * @return
	 */
	public List<LeaderData> aroundMe(String member, boolean useZeroIndexForRank) {
		return aroundMeIn(_leaderboardName, member, useZeroIndexForRank, _pageSize);
	}
	
	/**
	 * 
	 * @param leaderboardName
	 * @param member
	 * @param useZeroIndexForRank
	 * @param pageSize
	 * @return
	 */
	public List<LeaderData> aroundMeIn(String leaderboardName, String member, boolean useZeroIndexForRank, int pageSize) {
		long reverseRankForMember = _jedis.zrevrank(leaderboardName, member);
		
		if (pageSize < 1) {
			pageSize = DEFAULT_PAGE_SIZE;
		}

		int startingOffset = (int) reverseRankForMember - (pageSize / 2);
		if (startingOffset < 0) {
			startingOffset = 0;
		}
		int endingOffset = (startingOffset + pageSize) - 1;
	
		Set<Tuple> rawLeaderData = _jedis.zrevrangeWithScores(leaderboardName, startingOffset, endingOffset);
		return massageLeaderData(leaderboardName, rawLeaderData, useZeroIndexForRank);	
	}
	
	/**
	 * 
	 * @param members
	 * @param useZeroIndexForRank
	 * @return
	 */
	public List<LeaderData> rankedInList(List<String> members, boolean useZeroIndexForRank) {
		return rankedInListIn(_leaderboardName, members, useZeroIndexForRank);
	}
	
	/**
	 * 
	 * @param leaderboardName
	 * @param members
	 * @param useZeroIndexForRank
	 * @return
	 */
	public List<LeaderData> rankedInListIn(String leaderboardName, List<String> members, boolean useZeroIndexForRank) {
		List<LeaderData> leaderData = new ArrayList<LeaderData>();
		
		Iterator<String> membersIterator = members.iterator();
		while (membersIterator.hasNext()) {
			String member = membersIterator.next();
			LeaderData memberData = new LeaderData(member, scoreForIn(leaderboardName, member), rankForIn(leaderboardName, member, useZeroIndexForRank));
			leaderData.add(memberData);
		}
		
		return leaderData;
	}
		
	/**
	 * 
	 * @param leaderboardName
	 * @param memberData
	 * @param useZeroIndexForRank
	 * @return
	 */
	private List<LeaderData> massageLeaderData(String leaderboardName, Set<Tuple> memberData, boolean useZeroIndexForRank) {
		List<LeaderData> leaderData = new ArrayList<LeaderData>();
		
		Iterator<Tuple> memberDataIterator = memberData.iterator();
		while (memberDataIterator.hasNext()) {
			Tuple memberDataTuple = memberDataIterator.next();
			LeaderData leaderDataItem = new LeaderData(memberDataTuple.getElement(), memberDataTuple.getScore(), rankForIn(leaderboardName, memberDataTuple.getElement(), useZeroIndexForRank));
			leaderData.add(leaderDataItem);
		}
		
		return leaderData;
	}
}