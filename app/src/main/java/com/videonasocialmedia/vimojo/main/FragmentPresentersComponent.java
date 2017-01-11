package com.videonasocialmedia.vimojo.main;

import com.videonasocialmedia.vimojo.main.internals.di.PerFragment;
import com.videonasocialmedia.vimojo.main.modules.FragmentPresentersModule;
import com.videonasocialmedia.vimojo.settings.presentation.views.fragment.SettingsFragment;

import dagger.Component;

/**
 * Created by alvaro on 11/01/17.
 */

@PerFragment
@Component(dependencies = {SystemComponent.class}, modules = {FragmentPresentersModule.class})
public interface FragmentPresentersComponent {
  void inject(SettingsFragment fragment);
}
