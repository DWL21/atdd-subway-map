package wooteco.subway.domain.line.section.rule.SectionAddRule;

import wooteco.subway.domain.line.section.Section;
import wooteco.subway.domain.line.value.line.LineId;
import wooteco.subway.domain.line.value.section.Distance;
import wooteco.subway.domain.line.value.section.SectionId;
import wooteco.subway.domain.station.value.StationId;
import wooteco.subway.exception.line.SectionLengthException;

import java.util.List;
import java.util.Optional;

public class DownStationExists implements SectionAddRule {

    @Override
    public boolean isSatisfiedBy(List<Section> sections, Section section) {
        Optional<Section> wrappedSelectedSection = getSectionThatHasSameDownStationFromSections(sections, section);

        return wrappedSelectedSection.isPresent();
    }

    @Override
    public void execute(List<Section> sections, Section section) {
        Section selectedSection = getSectionThatHasSameDownStationFromSections(sections, section)
                .orElseThrow(IllegalStateException::new);

        validateThatSectionDistanceIsLowerThenExistingSection(section, selectedSection);

        Section newSection = new Section(
                new SectionId(selectedSection.getId()),
                new LineId(selectedSection.getLineId()),
                new StationId(selectedSection.getUpStationId()),
                new StationId(section.getUpStationId()),
                new Distance(selectedSection.getDistance() - section.getDistance())
        );

        sections.remove(selectedSection);
        sections.add(section);
        sections.add(newSection);
    }

    private Optional<Section> getSectionThatHasSameDownStationFromSections(List<Section> sections, Section sourceSection) {
        return sections.stream()
                .filter(sourceSection::hasSameDownStationId)
                .findAny();
    }

    private void validateThatSectionDistanceIsLowerThenExistingSection(Section section, Section selectedSection) {
        if (selectedSection.getDistance() <= section.getDistance()) {
            throw new SectionLengthException();
        }
    }

}
