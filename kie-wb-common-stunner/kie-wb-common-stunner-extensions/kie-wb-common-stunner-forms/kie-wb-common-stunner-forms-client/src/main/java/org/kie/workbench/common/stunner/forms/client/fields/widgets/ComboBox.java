/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.forms.client.fields.widgets;

import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.ValueListBox;
import org.kie.workbench.common.stunner.forms.client.fields.util.ListBoxValues;
import org.kie.workbench.common.stunner.forms.client.fields.util.StringUtils;

import javax.inject.Inject;
import java.util.List;

public class ComboBox implements ComboBoxView.ComboBoxPresenter {

    protected ListBoxValues listBoxValues;

    protected boolean showCustomValues = false;

    protected String currentTextValue = "";

    protected boolean quoteStringValues;

    protected boolean addCustomValues = true;

    protected String customPrompt;

    ComboBoxView.ModelPresenter modelPresenter;

    protected boolean notifyModelChanges = false;

    @Inject
    ComboBoxView view;

    @Override
    public void init( final ComboBoxView.ModelPresenter modelPresenter, final boolean notifyModelChanges, final ValueListBox<String> listBox, final TextBox textBox,
                      final boolean quoteStringValues, final boolean addCustomValues,
                      final String customPrompt, final String placeholder ) {
        this.quoteStringValues = quoteStringValues;
        this.addCustomValues = addCustomValues;
        this.customPrompt = customPrompt;
        this.modelPresenter = modelPresenter;
        this.notifyModelChanges = notifyModelChanges;
        view.init( this, modelPresenter, listBox, textBox, placeholder );
    }

    @Override
    public String getValue() {
        return view.getValue();
    }

    @Override
    public void setListBoxValues( final ListBoxValues listBoxValues ) {
        this.listBoxValues = listBoxValues;
    }

    @Override
    public void setShowCustomValues( final boolean showCustomValues ) {
        this.showCustomValues = showCustomValues;
    }

    @Override
    public void setAddCustomValues( final boolean addCustomValues ) {
        this.addCustomValues = addCustomValues;
    }

    @Override
    public void setCurrentTextValue( String currentTextValue ) {
        this.currentTextValue = currentTextValue;
    }

    @Override
    public ListBoxValues getListBoxValues() {
        return listBoxValues;
    }

    @Override
    public void updateListBoxValues( String listBoxValue ) {
        if ( showCustomValues ) {
            List<String> updatedValues = listBoxValues.update( listBoxValue );
            view.setAcceptableValues( updatedValues );
        } else {
            List<String> values = listBoxValues.getAcceptableValuesWithoutCustomValues();
            view.setAcceptableValues( values );
        }
    }

    @Override
    public void listBoxValueChanged( String newValue ) {
        if ( customPrompt.equals( newValue ) ) {
            // "Custom..." selected, show textBox with empty value
            setListBoxValue( "" );
            setTextBoxValue( "" );
            view.setListBoxVisible( false );
            view.setTextBoxVisible( true );
            view.setTextBoxFocus( true );
        } else if ( newValue.startsWith( "*" ) ) {
            // Not a valid value
            setListBoxValue( "" );
            setTextBoxValue( "" );
        } else if ( newValue.startsWith( listBoxValues.getEditPrefix() ) ) {
            // "Edit <value> ..." selected, show textBox with appropriate value
            String value = view.getModelValue();
            setTextBoxValue( value );
            view.setListBoxVisible( false );
            view.setTextBoxVisible( true );
            view.setTextBoxFocus( true );
        } else if ( listBoxValues.isCustomValue( newValue ) ) {
            // A Custom value has been selected
            String textValue = listBoxValues.getValueForDisplayValue( newValue );
            if ( quoteStringValues ) {
                textValue = StringUtils.createUnquotedConstant( textValue );
            }
            setListBoxValue( newValue );
            setTextBoxValue( textValue );
            if ( notifyModelChanges ) {
                notifyModelChanged();
            }
        } else if ( newValue != null ) {
            // A non-custom value has been selected
            setListBoxValue( newValue );
            setTextBoxValue( "" );
            if ( notifyModelChanges ) {
                notifyModelChanged();
            }
        }
        updateListBoxValues( view.getListBoxValue() );
    }

    @Override
    public void textBoxValueChanged( String newValue ) {
        if ( newValue != null ) {
            if ( !quoteStringValues ) {
                newValue = newValue.trim();
            }
            if ( !newValue.isEmpty() ) {
                String nonCustomValue = listBoxValues.getNonCustomValueForUserString( newValue );
                if ( nonCustomValue != null ) {
                    setListBoxValue( nonCustomValue );
                    setTextBoxValue( "" );
                    currentTextValue = "";
                } else {
                    String oldValue = currentTextValue;
                    String displayValue = addCustomValueToListBoxValues( newValue, oldValue );
                    setTextBoxValue( newValue );
                    currentTextValue = newValue;
                    setListBoxValue( displayValue );
                }
            } else {
                // Set the value even if it's ""
                setTextBoxValue( newValue );
                setListBoxValue( newValue );
                currentTextValue = newValue;
            }
            if ( notifyModelChanges ) {
                notifyModelChanged();
            }
        }
        view.setTextBoxVisible( false );
        view.setListBoxVisible( true );
    }

    @Override
    public String addCustomValueToListBoxValues( String newValue, String oldValue ) {
        if ( quoteStringValues ) {
            newValue = StringUtils.createQuotedConstant( newValue );
            oldValue = StringUtils.createQuotedConstant( oldValue );
        }
        if ( addCustomValues ) {
            return listBoxValues.addCustomValue( newValue, oldValue );
        } else {
            return newValue;
        }
    }

    public void setTextBoxValue( String value ) {
        view.setTextBoxValue( value );
        view.setTextBoxModelValue( value );
    }

    public void setListBoxValue( String value ) {
        view.setListBoxValue( value );
        view.setListBoxModelValue( value );
    }

    public void notifyModelChanged() {
        modelPresenter.notifyModelChanged();
    }
}
