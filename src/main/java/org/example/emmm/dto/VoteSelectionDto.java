package org.example.emmm.dto;

import lombok.*;
import org.example.emmm.domain.VoteSelection;

public class VoteSelectionDto {
    @Getter
    public static class CreateSelectReqDto{
        private Long voteOptionId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreateSelectResDto{
        private Long voteSelectionId;
        private Long voteId;
        private Long voteOptionId;
        private Long userId;

        public static CreateSelectResDto from(VoteSelection vs){
            return builder()
                    .voteSelectionId(vs.getId())
                    .voteId(vs.getVote().getId())
                    .voteOptionId(vs.getVoteOption().getId())
                    .userId(vs.getUser().getId())
                    .build();
        }
    }

    @Getter
    public static class UpdateSelectReqDto{
        private Long voteOptionId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UpdateSelectResDto{
        private Long voteSelectionId;
        private Long voteId;
        private Long voteOptionId;
        private Long userId;

        public static UpdateSelectResDto from(VoteSelection vs){
            return builder()
                    .voteSelectionId(vs.getId())
                    .voteId(vs.getVote().getId())
                    .voteOptionId(vs.getVoteOption().getId())
                    .userId(vs.getUser().getId())
                    .build();
        }
    }
}
