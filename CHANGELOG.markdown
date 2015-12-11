# leaderboard 2.0.2

* Use passed-in argument for `leaderboardName` in `changeScoreForMemberIn` and `totalPagesIn`.
* Pull request #1 to that adds logic to cope with cases where no such user is present in the leaderboard.

# leaderboard 2.0.1 (2011-12-22)

* Change `addMember` to `rankMember`.
* Added `deleteLeaderboard` and `deleteLeaderboardNamed`.
* Added ability to initialize Leaderboard class with an existing Redis connection.