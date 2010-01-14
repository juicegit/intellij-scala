package org.jetbrains.plugins.scala.lang.refactoring.extractMethod;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorFactoryImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import org.jetbrains.plugins.scala.ScalaBundle;
import org.jetbrains.plugins.scala.ScalaFileType;
import org.jetbrains.plugins.scala.lang.psi.ScDeclarationSequenceHolder;
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.templates.ScTemplateBody;
import org.jetbrains.plugins.scala.lang.psi.types.ScType;
import org.jetbrains.plugins.scala.lang.refactoring.util.ScalaNamesUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventListener;

/**
 * User: Alexander Podkhalyuzin
 * Date: 11.01.2010
 */
public class ScalaExtractMethodDialog extends DialogWrapper {
  private JButton buttonOK;

  private String REFACTORING_NAME = ScalaBundle.message("extract.method.title");
  private JPanel contentPane;
  private JTextField methodNameTextField;
  private JRadioButton publicRadioButton;
  private JRadioButton protectedRadioButton;
  private JRadioButton privateRadioButton;
  private JTextField protectedTextField;
  private JTextField privateTextField;
  private JButton biggerScopeButton;
  private JButton smallerScopeButton;
  private JPanel scopePanel;

  private ScalaExtractMethodSettings settings = null;
  private Project myProject;
  private PsiElement[] myElements;
  private boolean myHasReturn;
  private Editor myScopeEditor;

  @Override
  protected void dispose() {
    EditorFactoryImpl.getInstance().releaseEditor(myScopeEditor);
    super.dispose();
  }

  private ScDeclarationSequenceHolder myScope;

  public ScalaExtractMethodDialog(Project project, PsiElement[] elements, boolean hasReturn) {
    super(project, true);

    myElements = elements;
    myProject = project;
    myHasReturn = hasReturn;

    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    setTitle(REFACTORING_NAME);
    init();
    myScope = PsiTreeUtil.getParentOfType(myElements[0], ScDeclarationSequenceHolder.class);
    myScopeEditor = EditorFactoryImpl.getInstance().createEditor(EditorFactoryImpl.getInstance().createDocument(myScope.getText()), project, ScalaFileType.SCALA_FILE_TYPE, true);
    setUpDialog();
    updateOkStatus();
  }

  @Override
  protected JComponent createCenterPanel() {
    return contentPane;
  }

  @Override
  protected JComponent createContentPane() {
    return contentPane;
  }

  private void updateOkStatus() {
    setOKActionEnabled(ScalaNamesUtil.isIdentifier(getMethodName()) &&
        (getProtectedEncloser().equals("") || ScalaNamesUtil.isIdentifier(getProtectedEncloser())) &&
        (getPrivateEncloser().equals("") || ScalaNamesUtil.isIdentifier(getPrivateEncloser()))
    );
  }

  private String getProtectedEncloser() {
    return protectedTextField.getText();
  }

  private String getPrivateEncloser() {
    return privateTextField.getText();
  }

  private void setUpDialog() {
    methodNameTextField.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent e) {
        updateOkStatus();
      }

      public void keyPressed(KeyEvent e) {
        updateOkStatus();
      }

      public void keyReleased(KeyEvent e) {
        updateOkStatus();
      }
    });

    ButtonGroup visibilityGroup = new ButtonGroup();
    visibilityGroup.add(privateRadioButton);
    visibilityGroup.add(protectedRadioButton);
    visibilityGroup.add(publicRadioButton);
    publicRadioButton.setSelected(true); //todo: ApplicationSettings?
    privateTextField.setEnabled(false);
    protectedTextField.setEnabled(false);

    privateRadioButton.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (privateRadioButton.isSelected()) {
          privateTextField.setEnabled(true);
        } else privateTextField.setEnabled(false);
      }
    });

    protectedRadioButton.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (protectedRadioButton.isSelected()) {
          protectedTextField.setEnabled(true);
        } else protectedTextField.setEnabled(false);
      }
    });

    scopePanel.add(myScopeEditor.getComponent(), new GridConstraints()); //todo: params for GridConstraints
  }

  private String getVisibility() {
    if (publicRadioButton.isSelected()) return "";
    else if (privateRadioButton.isSelected()) {
      if (getPrivateEncloser().equals("")) return "private ";
      else return "private[" + getPrivateEncloser() + "] ";
    } else {
      if (getProtectedEncloser().equals("")) return "protected ";
      else return "protected[" + getProtectedEncloser() + "] ";
    }
  }

  private String[] getParamNames() {
    return new String[0]; //todo:
  }

  @Override
  protected void doOKAction() {
    settings = new ScalaExtractMethodSettings(getMethodName(), getParamNames(), getParamTypes(), getReturnTypes(),
        getVisibility(), getScope(), getSibling(), myElements, myHasReturn);
    super.doOKAction();
  }

  private ScType[] getParamTypes() {
    return new ScType[0]; //todo:
  }

  private ScType[] getReturnTypes() {
    return new ScType[0]; //todo:
  }

  private String getMethodName() {
    return methodNameTextField.getText();
  }

  private PsiElement getScope() {
    return myScope;
  }

  private PsiElement getSibling() {
    PsiElement result = myElements[0];
    while (result.getParent() != null && result.getParent() != getScope()) result = result.getParent();
    return result;
  }

  public ScalaExtractMethodSettings getSettings() {
    return settings;
  }
}