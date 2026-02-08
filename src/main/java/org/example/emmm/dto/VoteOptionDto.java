package org.example.emmm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.emmm.domain.AgendaConfig;
import org.example.emmm.domain.Vote;
import org.example.emmm.domain.VoteOption;
import org.example.emmm.domain.VoteSelection;

import java.util.List;

public class VoteOptionDto {
    @Getter
    public static class CreateOptionReqDto {
        private String content;
    }

    @Getter
    @AllArgsConstructor
    public static class CreateOptionResDto {
        private VoteOptionDto.VoteOptionBlock voteOption;

        public static VoteOptionDto.CreateOptionResDto from(VoteOption voteOption) {
            return new VoteOptionDto.CreateOptionResDto(
                    VoteOptionDto.VoteOptionBlock.from(voteOption)
            );
        }
    }

    @Getter
    @AllArgsConstructor
    public static class DetailOptionResDto {
        private VoteDto.VoteBlock vote;
        private List<VoteOptionBlock> voteOptions;
        private VoteSelectBlock voteSelection;

        public static VoteOptionDto.DetailOptionResDto from(Vote v, List<VoteOption> vos, VoteSelection vs) {
            return new VoteOptionDto.DetailOptionResDto(
                    VoteDto.VoteBlock.from(v),
                    vos.stream()
                            .map(VoteOptionBlock::from)
                            .toList(),
                    VoteSelectBlock.from(vs)
            );
        }

    }

    @Getter
    public static class UpdateOptionReqDto{
        private String content;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class UpdateOptionResDto{
        private VoteOptionBlock voteOption;

        public static VoteOptionDto.UpdateOptionResDto from(VoteOption vo) {
            return new VoteOptionDto.UpdateOptionResDto(
                    VoteOptionBlock.from(vo)
            );
        }
    }


    @Getter
    @AllArgsConstructor
    public static class VoteOptionBlock {
        private Long voteOptionId;
        private Long voteId;
        private String content;
        private int selectCount;

        public static VoteOptionDto.VoteOptionBlock from(VoteOption vo) {
            return new VoteOptionDto.VoteOptionBlock(
                    vo.getId(),
                    vo.getVote().getId(),
                    vo.getContent(),
                    vo.getSelectCount()
            );
        }
    }

    @Getter
    @AllArgsConstructor
    public static class VoteSelectBlock {
        private Long voteOptionId;
        public static VoteSelectBlock from(VoteSelection vs) {
            return new VoteSelectBlock(
                    vs.getVoteOption().getId()
            );
        }

    }
}
