package com.agoragames.leaderboard;

public class LeaderData {

	private String _member;
	private Double _score;
	private Long _rank;
	
	public LeaderData(String member, double score, long rank) {
		_member = member;
		_score = score;
		_rank = rank;
	}
	
	public void setMember(String member) {
		_member = member;
	}
	
	public String getMember() {
		return _member;
	}
	
	public void setScore(double score) {
		_score = score;
	}
	
	public double getScore() {
		return _score;
	}
	
	public void setRank(long rank) {
		_rank = rank;
	}
	
	public long getRank() {
		return _rank;
	}
}
