package jb.dva.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import jb.dva.SoundInflection;
import jb.dva.SoundLibrary;
import jb.dva.SoundReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoundListModel extends AbstractListModel<SoundReference> {
    private static final long serialVersionUID = 1L;
    final static Logger logger = LogManager.getLogger(SoundListModel.class);
    private List<String> translatedKeys = new ArrayList<>();
    private SoundLibrary library;
    private final List<String> filters = new LinkedList<>();

    public void setSoundLibrary(SoundLibrary l) {
        this.library = l;
        updateListContents();
    }

    public void clearFilter() {
        filters.clear();
        updateListContents();
    }

    public void addFilter(String s) {
        filters.add(s.toLowerCase());
        updateListContents();
    }

    private void updateListContents() {
        translatedKeys.clear();
        Iterator<String> it = library.keySet().iterator();
        Set<String> bestFilterMatches = new HashSet<>();

        if (filters.size() == 0)
        {
            translatedKeys = new ArrayList<>(library.keySet());
        }
        else
        {
            while (it.hasNext()) {
                String s = it.next();
                for (String filter : filters) {
                    int filterScore = matchesFilter(filter, s);
                    /*if (!filterMatches.containsKey(filterScore)) {
                        filterMatches.put(filterScore, new HashSet<String>());
                    }
                    filterMatches.get(filterScore).add(s);
                    LinkedList<Integer> sortedKeys = new LinkedList(filterMatches.keySet());
                    Collections.sort(sortedKeys);
                    int highestScore = sortedKeys.getLast();
                    if (highestScore > 0) {
                        bestFilterMatches.addAll(filterMatches.get(highestScore));

                        // If found exact match, bail.
                        if (i == 0 && highestScore == filter.length()) break;
                    }*/
                    if (filterScore == filter.length()) {
                        bestFilterMatches.add(s);
                    }
                }
            }

            translatedKeys = new ArrayList<>(bestFilterMatches);
        }

        Collections.sort(translatedKeys);
        fireContentsChanged(this, 0, getSize()-1);
    }

    /** Returns true if the supplied string matches the current filter. */
    private int matchesFilter(String filter, String s) {
        if (s.toLowerCase().startsWith(filter)) {
            return filter.length();
        } else {
            for (int i = filter.length() - 2; i > 0; i--) {
                String filterPart = filter.substring(0, i);
                if (s.toLowerCase().startsWith(filterPart)) {
                    return filterPart.length();
                }
            }
        }
        return 0;
    }

    public int getSize() {
        return translatedKeys.size();
    }

    public SoundReference getElementAt(int index) {
        return library.get(translatedKeys.get(index));
    }

    public URL getURLAt(int index, int inflection) {
        SoundReference ref = getElementAt(index);
        if (inflection == SoundInflection.NONE) {
            logger.debug("SoundListModel.getURLAt: inflection N - {}", ref.regular);
            return ref.regular;
        } else if (inflection == SoundInflection.BEGINNING) {
            logger.debug("SoundListModel.getURLAt: inflection B - {}", ref.beginning);
            return ref.beginning;
        } else if (inflection == SoundInflection.REGULAR) {
            logger.debug("SoundListModel.getURLAt: inflection R - {}", ref.regular);
            return ref.regular;
        } else if (inflection == SoundInflection.ENDING) {
            logger.debug("SoundListModel.getURLAt: inflection E - {}", ref.ending);
            return ref.ending;
        }
        return null;
    }
}
