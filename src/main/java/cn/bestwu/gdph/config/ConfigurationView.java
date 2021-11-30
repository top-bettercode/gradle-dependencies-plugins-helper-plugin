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

import cn.bestwu.gdph.search.UtilKt;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class ConfigurationView {

  private JButton resetButton;
  private JCheckBox useNexus;
  private JTextField nexusSearchUrl;
  private JPanel dpPanel;
  private JTextField artifactoryUrl;
  private JCheckBox useArtifactory;
  private JTextField artiRepos;
  private javax.swing.JPasswordField artifactoryPassword;
  private JTextField artifactoryUsername;
  private JComboBox<String> aliRepo;
  private JCheckBox useAli;

  public ConfigurationView() {
    useNexus.addActionListener(
        actionEvent -> nexusSearchUrl.setEnabled(useNexus.isSelected())
    );
    useAli.addActionListener(
        actionEvent -> aliRepo.setEnabled(useAli.isSelected())
    );
    useArtifactory.addActionListener(
        actionEvent -> {
          artifactoryUrl.setEnabled(useArtifactory.isSelected());
          artiRepos.setEnabled(useArtifactory.isSelected());
          artifactoryUsername.setEnabled(useArtifactory.isSelected());
          artifactoryPassword.setEnabled(useArtifactory.isSelected());
        }
    );
    aliRepo.removeAllItems();
    aliRepo.addItem("all");
    List<String> aliRepos = UtilKt.getAliRepos();
    for (String repo : aliRepos) {
      aliRepo.addItem(repo);
    }
  }

  public JPanel getDpPanel() {
    return dpPanel;
  }

  public String getAliRepo() {
    return aliRepo.getSelectedItem().toString();
  }

  public void setAliRepo(String aliRepo) {
    this.aliRepo.setSelectedItem(aliRepo);
  }

  public boolean getUseAli() {
    return useAli.isSelected();
  }

  public void setUseAli(boolean useAli) {
    this.useAli.setSelected(useAli);
    aliRepo.setEnabled(useAli);
  }

  public String getNexusSearchUrl() {
    return nexusSearchUrl.getText().trim();
  }

  public void setNexusSearchUrl(String nexusSearchUrl) {
    this.nexusSearchUrl.setText(nexusSearchUrl);
  }


  public boolean getUseNexus() {
    return useNexus.isSelected();
  }

  public void setUseNexus(boolean selected) {
    useNexus.setSelected(selected);
    nexusSearchUrl.setEnabled(selected);
  }


  public String getArtifactoryUrl() {
    return artifactoryUrl.getText().trim();
  }

  public void setArtifactoryUrl(String artifactoryUrl) {
    this.artifactoryUrl.setText(artifactoryUrl);
  }

  public String getArtiRepos() {
    return artiRepos.getText().trim();
  }

  public void setArtiRepos(String artiRepos) {
    this.artiRepos.setText(artiRepos);
  }

  public String getArtifactoryPassword() {
    return new String(artifactoryPassword.getPassword());
  }

  public void setArtifactoryPassword(String artifactoryPassword) {
    this.artifactoryPassword.setText(artifactoryPassword);
  }

  public String getArtifactoryUsername() {
    return artifactoryUsername.getText();
  }

  public void setArtifactoryUsername(String artifactoryUsername) {
    this.artifactoryUsername.setText(artifactoryUsername);
  }

  public boolean getUseArtifactory() {
    return useArtifactory.isSelected();
  }

  public void setUseArtifactory(boolean useArtifactory) {
    this.useArtifactory.setSelected(useArtifactory);
    artifactoryUrl.setEnabled(useArtifactory);
    artiRepos.setEnabled(useArtifactory);
    artifactoryUsername.setEnabled(useArtifactory);
    artifactoryPassword.setEnabled(useArtifactory);
  }

  public JButton getResetButton() {
    return resetButton;
  }
}
