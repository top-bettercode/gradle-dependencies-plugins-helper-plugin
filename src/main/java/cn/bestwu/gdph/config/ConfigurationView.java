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
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author peter
 */
public class ConfigurationView {

  private JButton resetButton;
  private JCheckBox useNexus;
  private JTextField nexusSearchUrlField;
  private JPanel dpPanel;
  private JPanel mavenIndexPanel;
  private JCheckBox useMavenIndex;
  private JCheckBox useMavenCentral;
  private JTextField remoteRepository;
  private JCheckBox useLocalMavenIndex;
  private TextFieldWithBrowseButton indexParentDir;

  public ConfigurationView() {
    useNexus.addActionListener(
        actionEvent -> nexusSearchUrlField.setEnabled(useNexus.isSelected())
    );
    useLocalMavenIndex.addActionListener(
        actionEvent -> indexParentDir.setEnabled(useLocalMavenIndex.isSelected())
    );
    if (!UtilKt.supportMavenIndex()) {
      mavenIndexPanel.setVisible(false);
    }
    indexParentDir.addBrowseFolderListener(null, null, null,
        new FileChooserDescriptor(false, true, false, false, false, false),
        new TextComponentAccessor<JTextField>() {
          @Override
          public String getText(JTextField component) {
            return component.getText();
          }

          @Override
          public void setText(JTextField component, String text) {
            final int len = text.length();
            if (len > 0 && text.charAt(len - 1) == File.separatorChar) {
              text = text.substring(0, len - 1);
            }
            component.setText(text);
          }
        });
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

  public String getRemoteRepository() {
    return remoteRepository.getText();
  }

  public void setRemoteRepository(String remoteRepository) {
    this.remoteRepository.setText(remoteRepository);
  }

  public boolean getUseLocalMavenIndex() {
    return useLocalMavenIndex.isSelected();
  }

  public void setUseLocalMavenIndex(boolean useLocalMavenIndex) {
    this.useLocalMavenIndex.setSelected(useLocalMavenIndex);
    indexParentDir.setEnabled(useLocalMavenIndex);
  }

  public String getIndexParentDir() {
    return indexParentDir.getText();
  }

  public void setIndexParentDir(String indexParentDir) {
    this.indexParentDir.setText(indexParentDir);
  }

  public boolean getUseNexus() {
    return useNexus.isSelected();
  }

  public void setUseNexus(boolean selected) {
    useNexus.setSelected(selected);
    nexusSearchUrlField.setEnabled(selected);
  }

  public boolean getUseMavenIndex() {
    return useMavenIndex.isSelected();
  }

  public void setUseMavenIndex(boolean selected) {
    useMavenIndex.setSelected(selected);
  }

  public boolean getUseMavenCentral() {
    return useMavenCentral.isSelected();
  }

  public void setUseMavenCentral(boolean useMavenCentral) {
    this.useMavenCentral.setSelected(useMavenCentral);
  }

  public JButton getResetButton() {
    return resetButton;
  }
}
