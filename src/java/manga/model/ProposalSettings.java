package manga.model;

public class ProposalSettings {

    private int maxSubmitAttempts;
    private int minimumVoteQuorum;

    public int getMaxSubmitAttempts() {
        return maxSubmitAttempts;
    }

    public void setMaxSubmitAttempts(int maxSubmitAttempts) {
        this.maxSubmitAttempts = maxSubmitAttempts;
    }

    public int getMinimumVoteQuorum() {
        return minimumVoteQuorum;
    }

    public void setMinimumVoteQuorum(int minimumVoteQuorum) {
        this.minimumVoteQuorum = minimumVoteQuorum;
    }
}
