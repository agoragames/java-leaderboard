package com.agoragames.leaderboard;

import java.util.*;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.Transaction;

public class Leaderboard {

	public static final String VERSION = "2.0.2";
	public static final int DEFAULT_PAGE_SIZE = 25;
	public static final String DEFAULT_REDIS_HOST = "localhost";
	public static final int DEFAULT_REDIS_PORT = 6379;
  	public static final List<LeaderData> EMPTY_LEADER_DATA = Collections.emptyList();

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
		this(leaderboardName, pageSize, new Jedis(host, port));
	}

	/**
	 * Create a leaderboard with a given name, page size and existing Redis connection
	 *
	 * @param leaderboardName Name of the leaderboard
	 * @param pageSize Page size
	 * @param redisConnection Redis connection
	 */
	public Leaderboard(String leaderboardName, int pageSize, Jedis redisConnection) {
	    _leaderboardName = leaderboardName;
	    _pageSize = pageSize;

	    if (_pageSize < 1) {
	        _pageSize = DEFAULT_PAGE_SIZE;
	    }

	    _jedis = redisConnection;
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
	public long deleteLeaderboard() {
		return deleteLeaderboardNamed(_leaderboardName);
	}

	/**
	 * Get the page size
	 *
	 * @return Page size
	 */
	public long deleteLeaderboardNamed(String leaderboardName) {
		return _jedis.del(leaderboardName);
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

		return (int) Math.ceil((float) totalMembersIn(leaderboardName) / (float) pageSize);
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
	 * Rank a member in the current leaderboard
	 *
	 * @param member Member
	 * @param score Score
	 * @return
	 */
	public long rankMember(String member, double score) {
		return this.rankMemberIn(_leaderboardName, member, score);
	}

	/**
	 * Rank a member in the named leaderboard
	 *
	 * @param leaderboardName Leaderboard
	 * @param member Member
	 * @param score Score
	 * @return
	 */
	public long rankMemberIn(String leaderboardName, String member, double score) {
		return _jedis.zadd(leaderboardName, score, member);
	}

	/**
	 * Retrieve the score for a member in the current leaderboard
	 *
	 *
     * @param member Member
     * @return Member score
	 */
	public Double scoreFor(String member) {
		return scoreForIn(_leaderboardName, member);
	}

	/**
	 * Retrieve the score for a member in the named leaderboard
	 *
	 *
     * @param leaderboardName Leaderboard
     * @param member Member
     * @return Member score
	 */
	public Double scoreForIn(String leaderboardName, String member) {
		return _jedis.zscore(leaderboardName, member);
	}

	/**
	 * Change the score for a member by a certain delta in the current leaderboard
	 *
	 * @param member Member
	 * @param delta Score delta
	 * @return Updated score
	 */
	public double changeScoreFor(String member, double delta) {
		return changeScoreForMemberIn(_leaderboardName, member, delta);
	}

	/**
	 * Change the score for a member by a certain delta in the named leaderboard
	 *
	 * @param leaderboardName Leaderboard
	 * @param member Member
	 * @param delta Score delta
	 * @return Updated score
	 */
	public double changeScoreForMemberIn(String leaderboardName, String member, double delta) {
		return _jedis.zincrby(leaderboardName, delta, member);
	}

	/**
	 * Check to see if member is in the current leaderboard
	 *
	 * @param member Member
	 * @return true if member is in the current leaderboard, false otherwise
	 */
	public boolean checkMember(String member) {
		return checkMemberIn(_leaderboardName, member);
	}

	/**
	 * Check to see if member is in the named leaderboard
	 *
	 * @param leaderboardName Leaderboard
	 * @param member Member
	 * @return true if member is in the named leaderboard, false otherwise
	 */
	public boolean checkMemberIn(String leaderboardName, String member) {
		return !(_jedis.zscore(leaderboardName, member) == null);
	}

	/**
	 * Retrieve the rank for a member in the current leaderboard
	 *
	 *
     * @param member Member
     * @param useZeroIndexForRank Use zero-based index for rank
     * @return Rank for member in the current leaderboard
	 */
	public Long rankFor(String member, boolean useZeroIndexForRank) {
		return rankForIn(_leaderboardName, member, useZeroIndexForRank);
	}

	/**
	 * Retrieve the rank for a member in the named leaderboard
	 *
	 *
     * @param leaderboardName Leaderboard
     * @param member Member
     * @param useZeroIndexForRank Use zero-based index for rank
     * @return Rank for member in the named leaderboard
	 */
	public Long rankForIn(String leaderboardName, String member, boolean useZeroIndexForRank) {

        Long result = null;

        Long redisRank = _jedis.zrevrank(leaderboardName, member);

        if (redisRank != null) {
            if (useZeroIndexForRank) {
    			result = redisRank;
	    	} else {
		    	result = (redisRank + 1);
		    }
        }

        return result;
	}

	/**
	 * Remove members from the current leaderboard in a given score range
	 *
	 * @param minScore Minimum score
	 * @param maxScore Maximum score
	 * @return
	 */
	public long removeMembersInScoreRange(double minScore, double maxScore) {
		return removeMembersInScoreRangeIn(_leaderboardName, minScore, maxScore);
	}

	/**
	 * Remove members from the named leaderboard in a given score range
	 *
	 * @param leaderboardName Leaderboard
	 * @param minScore Minimum score
	 * @param maxScore Maximum score
	 * @return
	 */
	public long removeMembersInScoreRangeIn(String leaderboardName, double minScore, double maxScore) {
		return _jedis.zremrangeByScore(leaderboardName, minScore, maxScore);
	}

	/**
	 * Retrieve score and rank for a member in the current leaderboard
	 *
	 * @param member Member
	 * @param useZeroIndexForRank Use zero-based index for rank
	 * @return Score and rank for a member in the current leaderboard
	 */
	public Hashtable<String, Object> scoreAndRankFor(String member, boolean useZeroIndexForRank) {
		return scoreAndRankForIn(_leaderboardName, member, useZeroIndexForRank);
	}

	/**
	 * Retrieve score and rank for a member in the named leaderboard
	 *
	 * @param leaderboardName Leaderboard
	 * @param member Member
	 * @param useZeroIndexForRank Use zero-based index for rank
	 * @return Score and rank for a member in the named leaderboard
	 */
	public Hashtable<String, Object> scoreAndRankForIn(String leaderboardName, String member, boolean useZeroIndexForRank) {
		Hashtable<String, Object> data = new Hashtable<String, Object>();

		Transaction transaction = _jedis.multi();
        transaction.zscore(leaderboardName, member);
        transaction.zrevrank(leaderboardName, member);
        List<Object> response = transaction.exec();

		data.put("member", member);
		data.put("score", response.get(0));
		if (useZeroIndexForRank) {
		    data.put("rank", response.get(1));
		} else {
		    data.put("rank", (Long) response.get(1) + 1);
		}

		return data;
	}

	/**
	 * Retrieve a page of leaders as a list of LeaderData in the current leaderboard
	 *
	 * @param currentPage Page
	 * @param useZeroIndexForRank Use zero-based index for rank
	 * @return Page of leaders as a list of LeaderData in the current leaderboard
	 */
	public List<LeaderData> leadersIn(int currentPage, boolean useZeroIndexForRank) {
		return leadersIn(_leaderboardName, currentPage, useZeroIndexForRank, _pageSize);
	}

	/**
	 * Retrieve a page of leaders as a list of LeaderData in the named leaderboard
	 *
	 * @param leaderboardName Leaderboard
	 * @param currentPage Page
	 * @param useZeroIndexForRank Use zero-based index for rank
	 * @param pageSize Page size
	 * @return Page of leaders as a list of LeaderData in the named leaderboard
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
	 * Retrieve leaders around a given member in the current leaderboard as a list of LeaderData
	 *
	 * @param member Member
	 * @param useZeroIndexForRank Use zero-based index for rank
	 * @return Leaders around a given member in the current leaderboard as a list of LeaderData
	 */
	public List<LeaderData> aroundMe(String member, boolean useZeroIndexForRank) {
		return aroundMeIn(_leaderboardName, member, useZeroIndexForRank, _pageSize);
	}

	/**
	 * Retrieve leaders around a given member in the named leaderboard as a list of LeaderData
	 *
	 * @param leaderboardName Leaderboard
	 * @param member Member
	 * @param useZeroIndexForRank Use zero-based index for rank
	 * @param pageSize Page size
	 * @return Leaders around a given member in the named leaderboard as a list of LeaderData
	 */
	public List<LeaderData> aroundMeIn(String leaderboardName, String member, boolean useZeroIndexForRank, int pageSize) {
		Long reverseRankForMember = _jedis.zrevrank(leaderboardName, member);

        if (reverseRankForMember == null) {
            return EMPTY_LEADER_DATA;
        }


		if (pageSize < 1) {
			pageSize = DEFAULT_PAGE_SIZE;
		}

		int startingOffset = reverseRankForMember.intValue() - (pageSize / 2);
		if (startingOffset < 0) {
			startingOffset = 0;
		}
		int endingOffset = (startingOffset + pageSize) - 1;

		Set<Tuple> rawLeaderData = _jedis.zrevrangeWithScores(leaderboardName, startingOffset, endingOffset);
		return massageLeaderData(leaderboardName, rawLeaderData, useZeroIndexForRank);
	}

	/**
	 * Retrieve a list of LeaderData objects for a list of members in the current leaderboard
	 *
	 * @param members List of member names
	 * @param useZeroIndexForRank Use zero-based index for rank
	 * @return List of LeaderData objects for a list of members in the current leaderboard
	 */
	public List<LeaderData> rankedInList(List<String> members, boolean useZeroIndexForRank) {
		return rankedInListIn(_leaderboardName, members, useZeroIndexForRank);
	}

	/**
	 * Retrieve a list of LeaderData objects for a list of members in the named leaderboard
	 *
	 * @param leaderboardName Leaderboard
	 * @param members List of member names
	 * @param useZeroIndexForRank Use zero-based index for rank
	 * @return List of LeaderData objects for a list of members in the named leaderboard
	 */
	public List<LeaderData> rankedInListIn(String leaderboardName, List<String> members, boolean useZeroIndexForRank) {
		List<LeaderData> leaderData = new ArrayList<LeaderData>();

		Iterator<String> membersIterator = members.iterator();
		while (membersIterator.hasNext()) {
			String member = membersIterator.next();
            Double score = scoreForIn(leaderboardName, member);

            if (score != null) {

                long rank = rankForIn(leaderboardName, member, useZeroIndexForRank);
                LeaderData memberData = new LeaderData(member, score, rank);
		    	leaderData.add(memberData);
            }
		}

		return leaderData;
	}

	/**
	 * Massage the leaderboard data into LeaderData objects
	 *
	 * @param leaderboardName Leaderboard
	 * @param memberData Tuple of member and score
	 * @param useZeroIndexForRank Use zero-based index for rank
	 * @return List of LeaderData objects which contains member, score and rank
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