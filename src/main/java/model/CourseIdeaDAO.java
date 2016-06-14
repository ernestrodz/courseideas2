package model;

import java.util.List;

/**
 * Created by Ernest on 6/14/2016.
 */
public interface CourseIdeaDAO {
    boolean add(CourseIdea idea);

    List<CourseIdea> findAll();
}
