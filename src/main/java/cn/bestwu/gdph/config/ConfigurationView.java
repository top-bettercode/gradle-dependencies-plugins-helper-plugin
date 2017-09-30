/*
 * Copyright (c) 2017 Peter Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.bestwu.gdph.config;

import cn.bestwu.gdph.UtilKt;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class ConfigurationView {

  private JPanel nexusPanel;
  private JButton resetButton;
  private JCheckBox useNexus;
  private JTextField nexusSearchUrlField;
  private JPanel dpPanel;
  private JPanel mavenIndexPanel;
  private JCheckBox useMavenIndex;
  private JLabel showtip;
  //  private JButton addRemoteRepo;
  private Set<String> remoteRepositories = new HashSet<>();

  public ConfigurationView() {
    useNexus.addActionListener(
        actionEvent -> nexusSearchUrlField.setEnabled(useNexus.isSelected())
    );
//    useMavenIndex.addActionListener(
//        actionEvent -> addRemoteRepo.setEnabled(useMavenIndex.isSelected()));
//    addRemoteRepo.addActionListener(e -> {
//      final StringEditor repositoryEditor = new StringEditor(
//          "Add Maven Remote Repository", "", "", new EditValidator());
//      if (repositoryEditor.showAndGet()) {
//        remoteRepositories.add(new MavenString(repositoryEditor.getName(), null,
//            repositoryEditor.getValue(), null, null, null));
//      }
//    });
    if (!UtilKt.supportMavenIndex()) {
      mavenIndexPanel.setVisible(false);
    }
  }

  public JPanel getDpPanel() {
    return dpPanel;
  }


  public String getNexusSearchUrl() {
    return nexusSearchUrlField.getText();
  }

  public void setNexusSearchUrl(String nexusSearchUrl) {
    this.nexusSearchUrlField.setText(nexusSearchUrl);
  }

  public boolean getUseNexus() {
    return useNexus.isSelected();
  }

  public void setUseNexus(boolean selected) {
    useNexus.setSelected(selected);
    nexusSearchUrlField.setEnabled(selected);
  }

  public Set<String> getRemoteRepositories() {
    return remoteRepositories;
  }

  public void setRemoteRepositories(
      Set<String> remoteRepositories) {
    this.remoteRepositories = remoteRepositories;
  }

  public boolean getUseMavenIndex() {
    return useMavenIndex.isSelected();
  }

  public void setUseMavenIndex(boolean selected) {
    useMavenIndex.setSelected(selected);
//    addRemoteRepo.setEnabled(selected);
  }

  public JButton getResetButton() {
    return resetButton;
  }
}
