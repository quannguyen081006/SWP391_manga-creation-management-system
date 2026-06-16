package manga.service;

import manga.model.ProposalSettings;
import manga.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProposalSettingsService {

    public static final int DEFAULT_MAX_SUBMIT_ATTEMPTS = 2;
    public static final int DEFAULT_MINIMUM_VOTE_QUORUM = 3;

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    public ProposalSettings getSettings() {
        ProposalSettings settings = new ProposalSettings();
        settings.setMaxSubmitAttempts(getMaxSubmitAttempts());
        settings.setMinimumVoteQuorum(getMinimumVoteQuorum());
        return settings;
    }

    public int getMaxSubmitAttempts() {
        return systemSettingRepository.getInt(SystemSettingRepository.MAX_SUBMIT_ATTEMPTS, DEFAULT_MAX_SUBMIT_ATTEMPTS);
    }

    public int getMinimumVoteQuorum() {
        return systemSettingRepository.getInt(SystemSettingRepository.MINIMUM_VOTE_QUORUM, DEFAULT_MINIMUM_VOTE_QUORUM);
    }

    public void updateSettings(int maxSubmitAttempts, int minimumVoteQuorum) {
        if (maxSubmitAttempts < 1 || maxSubmitAttempts > 10) {
            throw new IllegalArgumentException("Resubmit count must be between 1 and 10");
        }
        if (minimumVoteQuorum < 1 || minimumVoteQuorum > 20) {
            throw new IllegalArgumentException("Minimum vote quorum must be between 1 and 20");
        }
        systemSettingRepository.setProposalSettings(maxSubmitAttempts, minimumVoteQuorum);
    }
}
