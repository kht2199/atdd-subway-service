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

	public void add(Section section) {
		this.sections.add(section);
	}

	public boolean isEmpty() {
		return this.sections.isEmpty();
	}

	public int size() {
		return this.sections.size();
	}

	public void remove(Section it) {
		this.sections.remove(it);
	}

	public List<Station> orderedStations() {
		List<Station> stations = new ArrayList<>();
		Station downStation = findUpStation();
		stations.add(downStation);

		while (downStation != null) {
			Station finalDownStation = downStation;
			Optional<Section> nextLineStation = sections.stream()
				.filter(it -> it.getUpStation() == finalDownStation)
				.findFirst();
			if (!nextLineStation.isPresent()) {
				break;
			}
			downStation = nextLineStation.get().getDownStation();
			stations.add(downStation);
		}
		return stations;
	}

	private Station findUpStation() {
		Station downStation = sections.get(0).getUpStation();
		while (downStation != null) {
			Station finalDownStation = downStation;
			Optional<Section> nextLineStation = sections.stream()
				.filter(it -> it.getDownStation() == finalDownStation)
				.findFirst();
			if (!nextLineStation.isPresent()) {
				break;
			}
			downStation = nextLineStation.get().getUpStation();
		}

		return downStation;
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
}
