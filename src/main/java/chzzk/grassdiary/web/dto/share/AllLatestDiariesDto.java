package chzzk.grassdiary.web.dto.share;

import chzzk.grassdiary.domain.diary.Diary;
import java.util.List;

public record AllLatestDiariesDto(
        LatestMetaDto meta,
        List<LatestDiariesDto> diaries
) {
    public static AllLatestDiariesDto of(List<Diary> diaries, boolean hasMore) {
        return new AllLatestDiariesDto(
                LatestMetaDto.of(diaries, hasMore),
                LatestDiariesDto.of(diaries)
        );
    }
}
