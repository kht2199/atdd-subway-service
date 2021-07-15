package nextstep.subway.line.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;

import nextstep.subway.station.domain.Station;

@Embeddable
public class Sections {

	@OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
	private final List<Section> sections = new ArrayList<>();

	public Sections() {}

	public Sections(Section section) {
		this.sections.add(section);
	}

	public boolean isEmpty() {
		return this.sections.isEmpty();
	}

	public int size() {
		return this.sections.size();
	}

	public List<Station> orderedStations() {
		List<Station> stations = new ArrayList<>();
		Station downStation = findUpStation();
		stations.add(downStation);

		while (downStation != null) {
			Optional<Section> nextLineStation = findUpStation(downStation);
			if (!nextLineStation.isPresent()) {
				break;
			}
			downStation = nextLineStation.get().getDownStation();
			stations.add(downStation);
		}
		return stations;
	}

	public Optional<Section> findMatchUpStations(Station upStation) {
		return this.sections.stream()
			.filter(it -> it.getUpStation() == upStation)
			.findFirst();
	}

	public Optional<Section> findMatchDownStation(Station downStation) {
		return this.sections.stream()
			.filter(it -> it.getDownStation() == downStation)
			.findFirst();
	}

	public boolean containsStation(Station station) {
		return this.sections.stream()
			.anyMatch(s -> s.getUpStation() == station || s.getDownStation() == station);
	}

	public Optional<Section> findUpStation(Station station) {
		return this.sections.stream()
			.filter(s -> s.getUpStation() == station)
			.findFirst();
	}

	public Optional<Section> findDownStation(Station station) {
		return this.sections.stream()
			.filter(s -> s.getDownStation() == station)
			.findFirst();
	}

	private Station findUpStation() {
		Station downStation = sections.get(0).getUpStation();
		while (downStation != null) {
			Optional<Section> nextLineStation = findDownStation(downStation);
			if (!nextLineStation.isPresent()) {
				break;
			}
			downStation = nextLineStation.get().getUpStation();
		}

		return downStation;
	}

	public void addSection(Section section) {
		if (sections.isEmpty()) {
			sections.add(section);
			return;
		}
		Station upStation = section.getUpStation();
		Station downStation = section.getDownStation();
		int distance = section.getDistance();
		boolean isUpStationExisted = containsStation(upStation);
		boolean isDownStationExisted = containsStation(downStation);

		if (isUpStationExisted && isDownStationExisted) {
			throw new RuntimeException("이미 등록된 구간 입니다.");
		}

		if (!isUpStationExisted && !isDownStationExisted) {
			throw new RuntimeException("등록할 수 없는 구간 입니다.");
		}

		if (isUpStationExisted) {
			findMatchUpStations(upStation)
				.ifPresent(it -> it.updateUpStation(downStation, distance));
		}

		if (isDownStationExisted) {
			findMatchDownStation(downStation)
				.ifPresent(it -> it.updateDownStation(upStation, distance));
		}
		sections.add(section);
	}

	public void removeStation(Line line, Station station) {
		Optional<Section> upLineStation = findUpStation(station);
		Optional<Section> downLineStation = findDownStation(station);

		upLineStation.ifPresent(sections::remove);
		downLineStation.ifPresent(sections::remove);

		if (upLineStation.isPresent() && downLineStation.isPresent()) {
			Station newUpStation = downLineStation.get().getUpStation();
			Station newDownStation = upLineStation.get().getDownStation();
			int newDistance = upLineStation.get().getDistance() + downLineStation.get().getDistance();
			sections.add(new Section(line, newUpStation, newDownStation, newDistance));
		}
	}
}
