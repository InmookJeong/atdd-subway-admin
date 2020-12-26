package nextstep.subway.line.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nextstep.subway.line.exception.SectionDuplicatedException;
import nextstep.subway.station.domain.Station;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Getter
@EqualsAndHashCode(of = {"upStation", "downStation"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Section {

    @ManyToOne
    @JoinColumn(name = "up_station_id")
    private Station upStation;

    @ManyToOne
    @JoinColumn(name = "down_station_id")
    private Station downStation;

    @Column(name = "distance")
    private int distance;

    private Section(final Station upStation, final Station downStation, final int distance) {
        this.upStation = Objects.requireNonNull(upStation);
        this.downStation = Objects.requireNonNull(downStation);
        this.distance = distance;
    }

    public static Section of(final Station upStation, final Station downStation, final int distance) {
        if (upStation.equals(downStation)) {
            String message = String.format("구간의 상행과 하행은 같은 역일 수 없습니다. (입력 역:%s)", upStation.getName());
            throw new SectionDuplicatedException(message);
        }
        return new Section(upStation, downStation, distance);
    }

    public boolean isUpStation(final Station station) {
        return this.upStation.equals(station);
    }

    public boolean isDownStation(final Station station) {
        return this.downStation.equals(station);
    }
}