package nextstep.subway.line.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;

@Embeddable
public class Sections {

	@OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
	private List<Section> sections = new ArrayList<>();

	public void add(Section section) {
		this.sections.add(section);
	}

	public boolean isEmpty() {
		return this.sections.isEmpty();
	}

	public List<Section> getSections() {
		return Collections.unmodifiableList(sections);
	}

	public Stream<Section> stream() {
		return sections.stream();
	}

	public Section get(int i) {
		return sections.get(i);
	}

	public int size() {
		return this.sections.size();
	}

	public void remove(Section it) {
		this.sections.remove(it);
	}
}
