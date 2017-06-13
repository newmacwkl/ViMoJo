package com.videonasocialmedia.vimojo.domain.editor;

import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoResolution;
import com.videonasocialmedia.vimojo.repository.project.ProjectRepository;

import javax.inject.Inject;

/**
 * Created by alvaro on 20/10/16.
 */

public class UpdateVideoResolutionToProjectUseCase {

    private Project currentProject;
    protected ProjectRepository projectRepository;

  /**
   * Default constructor with project repository argument.
   *
   * @param projectRepository the project repository.
   */
  @Inject public UpdateVideoResolutionToProjectUseCase(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void updateResolution(VideoResolution.Resolution resolution) {
        currentProject = Project.getInstance(null, null, null, null);
        currentProject.getProfile().setResolution(resolution);
        projectRepository.update(currentProject);
    }
}
