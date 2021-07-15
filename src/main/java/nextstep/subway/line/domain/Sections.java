package nextstep.subway.line.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
		LinkedList<Section> sectionLinkedList = new LinkedList<>(this.sections);
		Section firstSection = findFirstSection();
		sectionLinkedList.remove(firstSection);
		//
		List<Station> stations = new ArrayList<>();
		stations.add(firstSection.getUpStation());
		stations.add(firstSection.getDownStation());
		Section currentSection = firstSection;
		while (true) {
			Optional<Section> downSectionOptional = removeDownSection(sectionLinkedList, currentSection);
			if (!downSectionOptional.isPresent()) {
				break;
			}
			currentSection = downSectionOptional.get();
			stations.add(currentSection.getDownStation());
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
		Optional<Section> upSection = findUpStation(station);
		Optional<Section> downSection = findDownStation(station);

		upSection.ifPresent(sections::remove);
		downSection.ifPresent(sections::remove);

		if (upSection.isPresent() && downSection.isPresent()) {
			Station newUpStation = downSection.get().getUpStation();
			Station newDownStation = upSection.get().getDownStation();
			int newDistance = upSection.get().getDistance() + downSection.get().getDistance();
			sections.add(new Section(line, newUpStation, newDownStation, newDistance));
		}
	}

	protected Section findFirstSection() {
		LinkedList<Section> listForPerformance = new LinkedList<>(this.sections);
		Section section = listForPerformance.remove(0);
		while (true) {
			Optional<Section> upSection = removeUpSection(listForPerformance, section);
			if (!upSection.isPresent()) {
				return section;
			}
			section = upSection.get();
		}
	}

	protected static Optional<Section> removeDownSection(LinkedList<Section> sections, Section section) {
		Iterator<Section> iterator = sections.iterator();
		while(iterator.hasNext()) {
			Section next = iterator.next();
			if (section.isDownSection(next)) {
				iterator.remove();
				return Optional.of(next);
			}
		}
		return Optional.empty();
	}

	protected static Optional<Section> removeUpSection(List<Section> sections, Section section) {
		Iterator<Section> iterator = sections.iterator();
		while(iterator.hasNext()) {
			Section next = iterator.next();
			if (section.isUpSection(next)) {
				iterator.remove();
				return Optional.of(next);
			}
		}
		return Optional.empty();
	}

}
