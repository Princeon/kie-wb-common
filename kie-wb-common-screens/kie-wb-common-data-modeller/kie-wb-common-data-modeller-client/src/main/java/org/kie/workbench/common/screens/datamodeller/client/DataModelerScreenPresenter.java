/**
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datamodeller.client;

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.guvnor.common.services.shared.validation.model.ValidationMessage;
import org.guvnor.messageconsole.events.PublishBaseEvent;
import org.guvnor.messageconsole.events.PublishBatchMessagesEvent;
import org.guvnor.messageconsole.events.SystemMessage;
import org.guvnor.messageconsole.events.UnpublishMessagesEvent;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datamodeller.client.context.DataModelerWorkbenchContext;
import org.kie.workbench.common.screens.datamodeller.client.context.DataModelerWorkbenchFocusEvent;
import org.kie.workbench.common.screens.datamodeller.client.resources.i18n.Constants;
import org.kie.workbench.common.screens.datamodeller.client.util.DataModelerUtils;
import org.kie.workbench.common.screens.datamodeller.client.validation.JavaFileNameValidator;
import org.kie.workbench.common.screens.datamodeller.client.validation.ValidatorService;
import org.kie.workbench.common.screens.datamodeller.events.DataModelSaved;
import org.kie.workbench.common.screens.datamodeller.events.DataModelStatusChangeEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataModelerEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectChangeEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectCreatedEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectDeletedEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectFieldChangeEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectFieldCreatedEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectFieldDeletedEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectFieldSelectedEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectSelectedEvent;
import org.kie.workbench.common.screens.datamodeller.model.DataModelerError;
import org.kie.workbench.common.screens.datamodeller.model.EditorModelContent;
import org.kie.workbench.common.screens.datamodeller.model.GenerationResult;
import org.kie.workbench.common.screens.datamodeller.model.TypeInfoResult;
import org.kie.workbench.common.screens.datamodeller.security.DataModelerFeatures;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.screens.javaeditor.client.type.JavaResourceType;
import org.kie.workbench.common.screens.javaeditor.client.widget.EditJavaSourceWidget;
import org.kie.workbench.common.services.datamodeller.core.DataModel;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.JavaTypeInfo;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.kie.workbench.common.services.datamodeller.core.impl.JavaTypeInfoImpl;
import org.kie.workbench.common.widgets.client.popups.validation.ValidationPopup;
import org.kie.workbench.common.widgets.metadata.client.KieEditor;
import org.kie.workbench.common.widgets.metadata.client.KieEditorView;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartTitleDecoration;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.mvp.LockRequiredEvent;
import org.uberfire.client.views.pfly.multipage.PageImpl;
import org.uberfire.client.workbench.events.PlaceHiddenEvent;
import org.uberfire.ext.editor.commons.client.file.CommandWithFileNameAndCommitMessage;
import org.uberfire.ext.editor.commons.client.file.FileNameAndCommitMessage;
import org.uberfire.ext.editor.commons.client.file.popups.CopyPopUpPresenter;
import org.uberfire.ext.editor.commons.client.file.popups.RenamePopUpPresenter;
import org.uberfire.ext.widgets.common.client.resources.i18n.CommonConstants;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnFocus;
import org.uberfire.lifecycle.OnMayClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.workbench.events.NotificationEvent;
import org.uberfire.workbench.model.menu.Menus;

@Dependent
@WorkbenchEditor(identifier = "DataModelerEditor",
        supportedTypes = { JavaResourceType.class },
        priority = Integer.MAX_VALUE)
public class DataModelerScreenPresenter
        extends KieEditor {

    public interface DataModelerScreenView
            extends
            KieEditorView {

        void setContext( DataModelerContext context );

        void refreshTypeLists( boolean keepCurrentSelection );

        void showUsagesPopupForDeletion( String message,
                List<Path> paths,
                Command yesCommand,
                Command cancelCommand );

        void showUsagesPopupForRenaming( String message,
                List<Path> paths,
                Command yesCommand,
                Command cancelCommand );

        void showYesNoCancelPopup( String title,
                String message,
                Command yesCommand,
                String yesButtonText,
                ButtonType yesButtonType,
                Command noCommand,
                String noButtonText,
                ButtonType noButtonType );

        void showYesNoCancelPopup( String title,
                String message,
                Command yesCommand,
                Command noCommand );

        void showParseErrorsDialog( String title, String message, Command onCloseCommand );

        void setDomainContainerTitle( String title, String tooltip );

        void redraw();
    }

    protected DataModelerScreenView view;

    @Inject
    protected EditJavaSourceWidget javaSourceEditor;

    @Inject
    protected Event<DataModelerEvent> dataModelerEvent;

    @Inject
    protected Event<UnpublishMessagesEvent> unpublishMessagesEvent;

    @Inject
    protected Event<PublishBatchMessagesEvent> publishBatchMessagesEvent;

    @Inject
    protected Event<LockRequiredEvent> lockRequired;

    @Inject
    protected Event<DataModelerWorkbenchFocusEvent> dataModelerFocusEvent;

    @Inject
    protected Caller<DataModelerService> modelerService;

    @Inject
    protected ValidatorService validatorService;

    @Inject
    protected JavaFileNameValidator javaFileNameValidator;

    @Inject
    protected JavaResourceType resourceType;

    @Inject
    protected DataModelerWorkbenchContext dataModelerWBContext;

    @Inject
    protected AuthorizationManager authorizationManager;

    protected DataModelerContext context;

    protected boolean uiStarted = false;

    protected boolean loading = false;

    private boolean loadTypesInfo = false;

    private SessionInfo sessionInfo;

    private String currentMessageType;

    private Integer originalSourceHash = null;

    private boolean sourceEditionEnabled = false;

    private static final int EDITABLE_SOURCE_TAB = 2;

    private static int editorIds = 0;

    private String editorId;

    @WorkbenchPartTitle
    public String getTitleText() {
        return super.getTitleText();
    }

    @WorkbenchPartTitleDecoration
    public IsWidget getTitle() {
        return super.getTitle();
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return super.getWidget();
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

    @Inject
    public DataModelerScreenPresenter( DataModelerScreenView baseView,
                                       SessionInfo sessionInfo ) {
        super( baseView );
        view = baseView;
        this.sessionInfo = sessionInfo;
        editorId = sessionInfo.getId() + "-" + editorIds++;
    }

    @OnStartup
    public void onStartup( final ObservablePath path,
                           final PlaceRequest place ) {

        loading = true;
        loadTypesInfo = !dataModelerWBContext.isTypesInfoLoaded();
        context = new DataModelerContext( editorId );
        setSourceEditionGrant();
        init( path, place, resourceType );

        currentMessageType = "DataModeler" + path.toURI();
        cleanSystemMessages( getCurrentMessageType() );

        javaSourceEditor.addChangeHandler( new EditJavaSourceWidget.TextChangeHandler() {
            @Override
            public void onTextChange() {
                if ( context != null ) {
                    context.setEditionStatus( DataModelerContext.EditionStatus.SOURCE_CHANGED );
                }
            }
        } );
    }

    @OnFocus
    public void onFocus() {
        if ( !loading && context != null ) {
            view.redraw();
            setActiveContext();
            showDataModellerDocks();
        }
    }

    private void showDataModellerDocks() {
        dataModelerFocusEvent.fire( new DataModelerWorkbenchFocusEvent() );
    }

    private void hideDataModellerDocks( @Observes PlaceHiddenEvent event ) {
        if ( context != null ) {
            if ( "DataModelerEditor".equals( event.getPlace().getIdentifier() ) &&
                    place != null && place.equals( event.getPlace() ) ) {
                dataModelerFocusEvent.fire( new DataModelerWorkbenchFocusEvent().lostFocus() );
            }
        }
    }

    @OnMayClose
    public boolean onMayClose() {
        if ( isDirty() ) {
            return view.confirmClose();
        }
        return true;
    }

    @OnClose
    public void OnClose() {
        versionRecordManager.clear();
        cleanSystemMessages( getCurrentMessageType() );
        clearContext();
        super.OnClose();
        dataModelerWBContext.clearContext();
    }

    private void setActiveContext() {
        dataModelerWBContext.setActiveContext( context );

        if ( context.getDataObject() != null && context.getObjectProperty() != null ) {
            refreshTitle( context.getDataObject(), context.getObjectProperty() );
        } else if ( context.getDataObject() != null ) {
            refreshTitle( context.getDataObject() );
        }
    }

    private void onSafeDelete() {

        if ( context.getEditorModelContent().getOriginalClassName() != null ) {
            //if we are about to delete a .java file that could be parsed without errors, and we can calculate the
            //className then we can check for class usages prior to deletion.

            final String className = context.getEditorModelContent().getOriginalClassName();
            modelerService.call( new RemoteCallback<List<Path>>() {

                @Override
                public void callback( List<Path> paths ) {

                    if ( paths != null && paths.size() > 0 ) {
                        //If usages for this class were detected in project assets
                        //show the confirmation message to the user.

                        view.showUsagesPopupForDeletion(
                                Constants.INSTANCE.modelEditor_confirm_deletion_of_used_class( className ),
                                paths,
                                new Command() {
                                    @Override
                                    public void execute() {
                                        onDelete( versionRecordManager.getPathToLatest() );
                                    }
                                },
                                new Command() {
                                    @Override
                                    public void execute() {
                                        //do nothing.
                                    }
                                } );

                    } else {
                        //no usages, just proceed with the deletion.
                        onDelete( versionRecordManager.getPathToLatest() );
                    }
                }
            } ).findClassUsages( versionRecordManager.getPathToLatest(), className );
        } else {
            //we couldn't parse the class, so no check can be done. Just proceed with the standard
            //file deletion procedure.
            onDelete( versionRecordManager.getPathToLatest() );
        }
    }

    private void onDelete( final Path path ) {
        deletePopUpPresenter.show( new ParameterizedCommand<String>() {
            @Override
            public void execute( final String comment ) {
                view.showBusyIndicator( org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.Deleting() );
                modelerService.call( getDeleteSuccessCallback(), new DataModelerErrorCallback( Constants.INSTANCE.modelEditor_deleting_error() ) ).delete( path,
                                                                                                                                                           context.getDataObject(),
                                                                                                                                                           comment );
            }
        } );
    }

    private void onCopy() {
        copyPopUpPresenter.show( versionRecordManager.getCurrentPath(),
                                 javaFileNameValidator,
                                 new CommandWithFileNameAndCommitMessage() {
                                     @Override
                                     public void execute( final FileNameAndCommitMessage details ) {
                                         view.showBusyIndicator( org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.Copying() );
                                         modelerService.call( getCopySuccessCallback( copyPopUpPresenter.getView() ),
                                                              getCopyErrorCallback( copyPopUpPresenter.getView() ) ).copy( versionRecordManager.getCurrentPath(),
                                                                                                                           details.getNewFileName(),
                                                                                                                           copyPopUpPresenter.getView().getPackageName(),
                                                                                                                           copyPopUpPresenter.getView().getTargetPath(),
                                                                                                                           details.getCommitMessage(),
                                                                                                                           true );
                                     }
                                 } );
    }

    protected DataModelerErrorCallback getCopyErrorCallback( final CopyPopUpPresenter.View copyPopupView ) {
        return new DataModelerErrorCallback( Constants.INSTANCE.modelEditor_copying_error() ) {

            @Override
            public boolean error( final Message message,
                                  final Throwable throwable ) {
                copyPopupView.hide();
                return super.error( message, throwable );
            }
        };
    }

    private void onSafeRename() {

        if ( context.getEditorModelContent().getOriginalClassName() != null ) {
            //if we are about to rename a .java file that could be parsed without errors, and we can calculate the
            //className we can check for class usages prior to renaming and we can also suggest to perform an automatic
            // class renaming.

            final String className = context.getEditorModelContent().getOriginalClassName();

            modelerService.call( new RemoteCallback<List<Path>>() {

                @Override
                public void callback( List<Path> paths ) {

                    if ( paths != null && paths.size() > 0 ) {
                        //If usages for this class were detected in project assets
                        //show the confirmation message to the user.

                        view.showUsagesPopupForRenaming(
                                Constants.INSTANCE.modelEditor_confirm_renaming_of_used_class( className ),
                                paths,
                                new Command() {
                                    @Override
                                    public void execute() {
                                        rename();
                                    }
                                },
                                new Command() {
                                    @Override
                                    public void execute() {
                                        //do nothing.
                                    }
                                }
                        );

                    } else {
                        //no usages, just proceed with the deletion.
                        rename();
                    }
                }
            } ).findClassUsages( versionRecordManager.getPathToLatest(), className );
        } else {
            //we couldn't parse the class, so no check can be done. Just proceed with the standard
            //file renaming procedure.
            rename();
        }
    }

    protected void rename() {
        if ( isDirty() ) {
            view.showYesNoCancelPopup( CommonConstants.INSTANCE.Information(),
                    Constants.INSTANCE.modelEditor_confirm_save_before_rename(),
                    new Command() {
                        @Override
                        public void execute() {
                            rename( true );
                        }
                    },
                    new Command() {
                        @Override
                        public void execute() {
                            rename( false );
                        }
                    } );
        } else {
            //just rename.
            rename( false );
        }
    }

    protected Command onValidate() {
        return new Command() {
            @Override
            public void execute() {

                //at validation time we must do the same calculation as if we were about to save.
                final DataObject[] modifiedDataObject = new DataObject[ 1 ];
                if ( isDirty() ) {
                    if ( context.isEditorChanged() ) {

                        //at save time the source has always priority over the model.
                        //If the source was properly parsed and the editor has changes, we need to send the DataObject
                        //to the server in order to let the source to be updated prior to save.
                        modifiedDataObject[ 0 ] = context.getDataObject();
                    } else {
                        //if the source has changes, no update form the UI to the source will be performed.
                        //instead the parsed DataObject must be returned from the server.
                        modifiedDataObject[ 0 ] = null;
                    }
                }

                modelerService.call( new RemoteCallback<List<ValidationMessage>>() {
                    @Override
                    public void callback( final List<ValidationMessage> results ) {
                        if ( results == null || results.isEmpty() ) {
                            notification.fire( new NotificationEvent( org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.ItemValidatedSuccessfully(),
                                                                      NotificationEvent.NotificationType.SUCCESS ) );
                        } else {
                            ValidationPopup.showMessages( results );
                        }
                    }
                } ).validate( getSource(), versionRecordManager.getCurrentPath(), modifiedDataObject[ 0 ] );
            }
        };
    }

    private boolean isDirty() {
        return isDataObjectDirty() || isSourceDirty();
    }

    private boolean isDataObjectDirty() {
        return isDirty( context.getDataObject() != null ? context.getDataObject().hashCode() : null );
    }

    private boolean isSourceDirty() {
        return originalSourceHash != null && originalSourceHash != getSource().hashCode();
    }

    private RemoteCallback<Path> getCopySuccessCallback( final CopyPopUpPresenter.View copyPopupView ) {
        return new RemoteCallback<Path>() {

            @Override
            public void callback( final Path response ) {
                copyPopupView.hide();
                view.hideBusyIndicator();
                notification.fire( new NotificationEvent( org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.ItemCopiedSuccessfully(),
                        NotificationEvent.NotificationType.SUCCESS ) );
            }
        };
    }

    private RemoteCallback<Path> getDeleteSuccessCallback() {
        return new RemoteCallback<Path>() {

            @Override
            public void callback( final Path response ) {
                view.hideBusyIndicator();
                notification.fire( new NotificationEvent( org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.ItemDeletedSuccessfully(),
                                                          NotificationEvent.NotificationType.SUCCESS ) );
            }
        };
    }

    private RemoteCallback<Path> getRenameSuccessCallback( final RenamePopUpPresenter.View renamePopupView ) {
        return new RemoteCallback<Path>() {
            @Override
            public void callback( final Path targetPath ) {
                renamePopupView.hide();
                view.hideBusyIndicator();
                notification.fire( new NotificationEvent( org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.ItemRenamedSuccessfully(),
                                                          NotificationEvent.NotificationType.SUCCESS ) );
            }
        };
    }

    protected void save() {

        final JavaTypeInfoImpl newTypeInfo = new JavaTypeInfoImpl();
        if ( isDirty() ) {
            if ( context.isEditorChanged() ) {
                newTypeInfo.setPackageName( context.getDataObject().getPackageName() );
                newTypeInfo.setName( context.getDataObject().getName() );
                saveFile( newTypeInfo );
            } else {
                view.showLoading();
                modelerService.call( new RemoteCallback<TypeInfoResult>() {
                    @Override
                    public void callback( TypeInfoResult typeInfoResult ) {
                        view.hideBusyIndicator();
                        if ( !typeInfoResult.hasErrors() && typeInfoResult.getJavaTypeInfo() != null ) {
                            newTypeInfo.setPackageName( typeInfoResult.getJavaTypeInfo().getPackageName() );
                            newTypeInfo.setName( typeInfoResult.getJavaTypeInfo().getName() );
                            saveFile( newTypeInfo );
                        } else {
                            saveFile( null );
                        }
                    }
                } ).loadJavaTypeInfo( getSource() );
            }
        } else {
            saveFile( null );
        }
    }

    private void saveFile( final JavaTypeInfo newTypeInfo ) {

        String currentFileName = DataModelerUtils.extractSimpleFileName( versionRecordManager.getPathToLatest() );

        if ( hasPackageNameChanged( newTypeInfo ) ) {

            view.showYesNoCancelPopup( CommonConstants.INSTANCE.Information(),
                    Constants.INSTANCE.modelEditor_confirm_file_package_refactoring( newTypeInfo.getPackageName() ),
                    new Command() {
                        @Override
                        public void execute() {
                            savePopUpPresenter.show( versionRecordManager.getPathToLatest(), getSaveCommand( newTypeInfo, versionRecordManager.getPathToLatest() ) );
                        }
                    },
                    Constants.INSTANCE.modelEditor_action_yes_refactor_directory(),
                    ButtonType.PRIMARY,
                    new Command() {
                        @Override
                        public void execute() {
                            savePopUpPresenter.show( versionRecordManager.getPathToLatest(), getSaveCommand( versionRecordManager.getPathToLatest() ) );
                        }
                    },
                    Constants.INSTANCE.modelEditor_action_no_dont_refactor_directory(),
                    ButtonType.DANGER );


        } else if ( hasFileNameChanged( newTypeInfo, currentFileName ) ) {

            view.showYesNoCancelPopup( CommonConstants.INSTANCE.Information(),
                    Constants.INSTANCE.modelEditor_confirm_file_name_refactoring( newTypeInfo.getName() ),
                    new Command() {
                        @Override
                        public void execute() {
                            savePopUpPresenter.show( versionRecordManager.getPathToLatest(), getSaveCommand( newTypeInfo, versionRecordManager.getPathToLatest() ) );
                        }
                    },
                    Constants.INSTANCE.modelEditor_action_yes_refactor_file_name(),
                    ButtonType.PRIMARY,
                    new Command() {
                        @Override
                        public void execute() {
                            savePopUpPresenter.show( versionRecordManager.getPathToLatest(), getSaveCommand( versionRecordManager.getPathToLatest() ) );
                        }
                    },
                    Constants.INSTANCE.modelEditor_action_no_dont_refactor_file_name(),
                    ButtonType.DANGER );

        } else {
            savePopUpPresenter.show( versionRecordManager.getPathToLatest(), getSaveCommand( versionRecordManager.getPathToLatest() ) );
        }
    }

    private boolean hasFileNameChanged( JavaTypeInfo newTypeInfo,
                                        String currentFileName ) {
        return currentFileName != null && newTypeInfo != null && newTypeInfo.getName() != null && !currentFileName.equals( newTypeInfo.getName() );
    }

    private boolean hasPackageNameChanged( JavaTypeInfo newTypeInfo ) {
        return newTypeInfo != null && newTypeInfo.getPackageName() != null && !newTypeInfo.getPackageName().equals( context.getEditorModelContent().getOriginalPackageName() );
    }

    private ParameterizedCommand<String> getSaveCommand( final Path path ) {
        return getSaveCommand( null, path );
    }

    private ParameterizedCommand<String> getSaveCommand( final JavaTypeInfo newTypeInfo,
                                                         final Path path ) {
        return new ParameterizedCommand<String>() {
            @Override
            public void execute( final String commitMessage ) {

                final DataObject[] modifiedDataObject = new DataObject[ 1 ];
                if ( isDirty() ) {
                    if ( context.isEditorChanged() ) {

                        //at save time the source has always priority over the model.
                        //If the source was properly parsed and the editor has changes, we need to send the DataObject
                        //to the server in order to let the source to be updated prior to save.
                        modifiedDataObject[ 0 ] = context.getDataObject();
                    } else {
                        //if the source has changes, no update form the UI to the source will be performed.
                        //instead the parsed DataObject must be returned from the server.
                        modifiedDataObject[ 0 ] = null;
                    }
                }
                view.showSaving();

                if ( newTypeInfo != null ) {
                    modelerService.call( getSaveSuccessCallback( newTypeInfo, path ),
                                         new DataModelerErrorCallback( Constants.INSTANCE.modelEditor_saving_error() ) ).saveSource(
                            getSource(),
                            path,
                            modifiedDataObject[ 0 ],
                            metadata, commitMessage,
                            newTypeInfo.getPackageName(), newTypeInfo.getName() );
                } else {
                    modelerService.call( getSaveSuccessCallback( newTypeInfo, path ),
                                         new DataModelerErrorCallback( Constants.INSTANCE.modelEditor_saving_error() ) ).saveSource(
                            getSource(),
                            path,
                            modifiedDataObject[ 0 ],
                            metadata, commitMessage );
                }

            }
        };
    }

    private RemoteCallback<GenerationResult> getSaveSuccessCallback( final JavaTypeInfo newTypeInfo,
                                                                     final Path currentPath ) {
        return new RemoteCallback<GenerationResult>() {

            @Override
            public void callback( GenerationResult result ) {

                view.hideBusyIndicator();

                if ( newTypeInfo == null ) {

                    Boolean oldDirtyStatus = isDirty();

                    if ( result.hasErrors() ) {
                        context.setParseStatus( DataModelerContext.ParseStatus.PARSE_ERRORS );
                        updateEditorView( null );
                        context.setDataObject( null );

                        if ( isEditorTabSelected() ) {
                            //un common case
                            showParseErrorsDialog( Constants.INSTANCE.modelEditor_message_file_parsing_errors(),
                                                   true,
                                                   result.getErrors(),
                                                   getOnSaveParseErrorCommand() );
                        }
                    } else {
                        context.setParseStatus( DataModelerContext.ParseStatus.PARSED );
                        if ( context.isSourceChanged() ) {
                            updateEditorView( result.getDataObject() );
                            context.setDataObject( result.getDataObject() );
                        }
                        cleanSystemMessages( getCurrentMessageType() );
                    }

                    setSource( result.getSource() );

                    context.setEditionStatus( DataModelerContext.EditionStatus.NO_CHANGES );
                    setOriginalHash( context.getDataObject() != null ? context.getDataObject().hashCode() : null );
                    originalSourceHash = getSource().hashCode();

                    notification.fire( new NotificationEvent( org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.ItemSavedSuccessfully(), NotificationEvent.NotificationType.SUCCESS ) );
                    dataModelerEvent.fire( new DataModelStatusChangeEvent( context.getContextId(),
                                                                           DataModelerEvent.DATA_MODEL_BROWSER,
                                                                           oldDirtyStatus,
                                                                           false ) );

                    dataModelerEvent.fire( new DataModelSaved( context.getContextId(), null ) );

                    versionRecordManager.reloadVersions( currentPath );

                } else {
                    notification.fire( new NotificationEvent(
                            org.uberfire.ext.editor.commons.client.resources.i18n.CommonConstants.INSTANCE.ItemRenamedSuccessfully(),
                            NotificationEvent.NotificationType.SUCCESS ) );
                    //If the file was renamed as part of the file saving, don't do anything.
                    //A rename event will arrive, the same as for the "Rename" case.
                    //and the file will be automatically reloaded.
                }
            }
        };
    }

    @Override
    protected void loadContent() {
        modelerService.call( getLoadModelSuccessCallback(),
                getNoSuchFileExceptionErrorCallback() ).loadContent(
                versionRecordManager.getCurrentPath(), loadTypesInfo );
    }

    private RemoteCallback<EditorModelContent> getLoadModelSuccessCallback() {
        return new RemoteCallback<EditorModelContent>() {

            @Override
            public void callback( EditorModelContent content ) {

                //Path is set to null when the Editor is closed (which can happen before async calls complete).
                if ( versionRecordManager.getCurrentPath() == null ) {
                    return;
                }

                uiStarted = false;
                resetEditorPages( content.getOverview() );
                addSourceEditorPage();
                uiStarted = true;

                initContext( content );
                javaSourceEditor.setReadonly( isReadOnly || !sourceEditionEnabled );
                javaSourceEditor.setContent( content.getSource() );

                view.hideBusyIndicator();

                if ( content.hasErrors() ) {
                    publishSystemMessages( getCurrentMessageType(), true, content.getErrors() );
                }

                if ( content.getDataObject() != null ) {
                    selectEditorTab();
                } else {
                    showParseErrorsDialog( Constants.INSTANCE.modelEditor_message_file_parsing_errors(),
                                           false,
                                           context.getEditorModelContent().getErrors(),
                                           getOnLoadParseErrorCommand() );
                }

                showDataModellerDocks();
                setOriginalHash( context.getDataObject() != null ? context.getDataObject().hashCode() : null );
                originalSourceHash = getSource().hashCode();
                loading = false;
            }
        };
    }

    /**
     * This command is executed when a file with parse errors was initially loaded from server.
     */
    protected Command getOnLoadParseErrorCommand() {
        return new Command() {
            @Override
            public void execute() {
                selectSourceTab();
            }
        };
    }

    /**
     * This command is executed when a file that apparently is well is saved and the server returns parse errors.
     * Uncommon case.
     */
    protected Command getOnSaveParseErrorCommand() {
        return new Command() {
            @Override
            public void execute() {
                selectSourceTab();
            }
        };
    }

    /**
     * This command is executed every time the user changes the file source and the data is sent to server por parsing
     * during edition and there are parse errors. Typically when the user e.g. goes to the source tab, modifies the code
     * and returns to the editor tab.
     */
    protected Command getOnSourceParseErrorCommand() {
        return new Command() {
            @Override
            public void execute() {
                selectSourceTab();
            }
        };
    }

    /**
     * This command is executed when the editor tab is selected but there are parse errors.
     */
    protected Command getOnEditorTabSelectedWithParseErrorCommand() {

        return new Command() {
            @Override
            public void execute() {
                selectSourceTab();
            }
        };
    }

    protected void selectSourceTab() {
        setSelectedTab( EDITABLE_SOURCE_TAB );
    }

    private void addSourceEditorPage() {
        addPage( new PageImpl( javaSourceEditor,
                org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.SourceTabTitle() ) {
            @Override
            public void onFocus() {
                if ( uiStarted ) {
                    onSourceTabSelected();
                }
            }

            @Override
            public void onLostFocus() {
            }
        } );
    }

    private void rename( final boolean saveCurrentChanges ) {

        final DataObject[] modifiedDataObject = new DataObject[ 1 ];
        if ( saveCurrentChanges ) {
            if ( isDirty() ) {
                if ( context.isEditorChanged() ) {
                    //at save time the source has always priority over the model.
                    //If the source was properly parsed and the editor has changes, we need to send the DataObject
                    //to the server in order to let the source to be updated prior to save.
                    modifiedDataObject[ 0 ] = context.getDataObject();
                } else {
                    //if the source has changes, no update form the UI to the source will be performed.
                    //instead the parsed DataObject must be returned from the server.
                    modifiedDataObject[ 0 ] = null;
                }
            }
        }

        renamePopUpPresenter.show( versionRecordManager.getPathToLatest(),
                                   javaFileNameValidator,
                                   new CommandWithFileNameAndCommitMessage() {
                                       @Override
                                       public void execute( final FileNameAndCommitMessage details ) {
                                           view.showBusyIndicator( org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.Renaming() );

                                           modelerService.call( getRenameSuccessCallback( renamePopUpPresenter.getView() ),
                                                                getRenameErrorCallback( renamePopUpPresenter.getView() ) ).rename( versionRecordManager.getPathToLatest(),
                                                                                                                                   details.getNewFileName(),
                                                                                                                                   details.getCommitMessage(),
                                                                                                                                   true,
                                                                                                                                   saveCurrentChanges,
                                                                                                                                   getSource(), modifiedDataObject[ 0 ], metadata );
                                       }
                                   } );
    }

    protected DataModelerErrorCallback getRenameErrorCallback( final RenamePopUpPresenter.View renamePopupView ) {
        return new DataModelerErrorCallback( Constants.INSTANCE.modelEditor_renaming_error() ) {

            @Override
            public boolean error( final Message message,
                                  final Throwable throwable ) {
                renamePopupView.hide();
                return super.error( message, throwable );
            }
        };
    }

    public DataModel getDataModel() {
        return context.getDataModel();
    }

    public String getSource() {
        return javaSourceEditor.getContent();
    }

    public void setSource( String source ) {
        javaSourceEditor.setContent( source );
        context.getEditorModelContent().setSource( source );
    }

    private void setSourceEditionGrant() {
        sourceEditionEnabled = authorizationManager.authorize( DataModelerFeatures.EDIT_SOURCES, sessionInfo.getIdentity() );
    }

    private void initContext( EditorModelContent content ) {

        if ( loadTypesInfo ) {
            dataModelerWBContext.setAnnotationDefinitions( content.getAnnotationDefinitions() );
            dataModelerWBContext.setPropertyTypes( content.getPropertyTypes() );
        }

        context.setReadonly( isReadOnly );
        context.setEditorModelContent( content );
        context.setAnnotationDefinitions( dataModelerWBContext.getAnnotationDefinitions() );
        context.init( dataModelerWBContext.getPropertyTypes() );

        context.setEditionStatus( DataModelerContext.EditionStatus.NO_CHANGES );
        if ( content.getDataObject() != null ) {
            context.setParseStatus( DataModelerContext.ParseStatus.PARSED );
            context.setEditionMode( DataModelerContext.EditionMode.GRAPHICAL_MODE );
        } else {
            context.setParseStatus( DataModelerContext.ParseStatus.PARSE_ERRORS );
            context.setEditionMode( DataModelerContext.EditionMode.SOURCE_MODE );
        }
    }

    @Override
    public void onSourceTabSelected() {

        context.setEditionMode( DataModelerContext.EditionMode.SOURCE_MODE );
        if ( context.isParsed() && context.isEditorChanged() ) {

            //If there are changes in the ui the source must be regenerated on server side.
            view.showLoading();
            modelerService.call( new RemoteCallback<GenerationResult>() {
                @Override
                public void callback( GenerationResult result ) {
                    view.hideBusyIndicator();
                    setSource( result.getSource() );
                    updateSource( result.getSource() );
                    context.setEditionStatus( DataModelerContext.EditionStatus.NO_CHANGES );
                    setActiveContext();
                }
            }, new DataModelerErrorCallback( Constants.INSTANCE.modelEditor_loading_error() ) ).updateSource( getSource(), versionRecordManager.getCurrentPath(), context.getDataObject() );
        } else {
            if ( !isOverviewTabSelected() ) {
                context.setEditionStatus( DataModelerContext.EditionStatus.NO_CHANGES );
            }
            updateSource( javaSourceEditor.getContent() );
            setActiveContext();
        }
    }

    @Override
    public void onOverviewSelected() {
        context.setEditionMode( DataModelerContext.EditionMode.SOURCE_MODE );
        setActiveContext();
    }

    @Override
    protected void updateSource( String source ) {
        setSource( source );
    }

    private void updateEditorView( DataObject dataObject ) {
        //here we need to check if data object name, or package, changed, etc.
        //if this is the likely we can show an alert to the user, etc.
        //also the file should be renamed.

        if ( context.getDataObject() != null ) {
            context.getDataModel().removeDataObject( context.getDataObject().getClassName() );
        }
        if ( dataObject != null ) {
            context.getDataModel().removeDataObject( dataObject.getClassName() );
            context.getDataModel().addDataObject( dataObject );
        }
    }

    @Override
    public void onEditTabSelected() {
        if ( !uiStarted ) {
            return;
        }
        boolean doParsing = false;
        if ( context.isSourceChanged() ) {
            //if there has been changes in the source we should try to parse the file and build the data object again.
            doParsing = true;
        } else if ( context.isNotParsed() ) {
            //uncommon case, the file wasn't parsed yet.
            doParsing = true;
        }

        if ( doParsing ) {

            view.showLoading();

            //If there are changes in the source, we must try to parse the file.
            modelerService.call( new RemoteCallback<GenerationResult>() {
                @Override
                public void callback( GenerationResult result ) {
                    view.hideBusyIndicator();

                    if ( result.hasErrors() ) {

                        context.setParseStatus( DataModelerContext.ParseStatus.PARSE_ERRORS );
                        context.setEditionMode( DataModelerContext.EditionMode.SOURCE_MODE );
                        updateEditorView( null );
                        context.setDataObject( null );

                        showParseErrorsDialog( Constants.INSTANCE.modelEditor_message_file_parsing_errors(),
                                true,
                                result.getErrors(),
                                getOnSourceParseErrorCommand() );

                    } else {
                        //ok, we can reload the editor tab.
                        context.setParseStatus( DataModelerContext.ParseStatus.PARSED );
                        updateEditorView( result.getDataObject() );
                        context.setEditionStatus( DataModelerContext.EditionStatus.NO_CHANGES );
                        context.setDataObject( result.getDataObject() );
                        context.setObjectProperty( null );
                        context.setEditionMode( DataModelerContext.EditionMode.GRAPHICAL_MODE );
                        view.setContext( context );
                        cleanSystemMessages( getCurrentMessageType() );
                        setActiveContext();
                    }
                }
            }, new DataModelerErrorCallback( Constants.INSTANCE.modelEditor_loading_error() ) ).updateDataObject( context.getDataObject(), getSource(), versionRecordManager.getCurrentPath() );
        } else {
            //no changes in the source tab
            if ( !isOverviewTabSelected() ) {
                context.setEditionStatus( DataModelerContext.EditionStatus.NO_CHANGES );
            }

            if ( context.isParseErrors() ) {
                //there are parse errors, the editor tab couldn't be loaded.  (errors are already published)
                showParseErrorsDialog( Constants.INSTANCE.modelEditor_message_file_parsing_errors(),
                        false,
                        null,
                        getOnEditorTabSelectedWithParseErrorCommand() );
            } else {
                context.setEditionMode( DataModelerContext.EditionMode.GRAPHICAL_MODE );
                view.setContext( context );
                setActiveContext();
            }
        }
    }

    private void showParseErrorsDialog( final String message,
            final boolean publishErrors,
            final List<DataModelerError> errors,
            final Command command ) {

        if ( publishErrors && errors != null && !errors.isEmpty() ) {
            publishSystemMessages( getCurrentMessageType(), true, errors );
        }

        view.showParseErrorsDialog( CommonConstants.INSTANCE.Information(), message, command );
    }

    private void onDataObjectDeleted( @Observes DataObjectDeletedEvent event ) {
        if ( context != null &&
                event.isFrom( context.getCurrentProject() ) &&
                event.getCurrentDataObject() != null &&
                context.isParsed() &&
                isEditorTabSelected() &&
                context.getDataObject() != null &&
                !context.getDataObject().getClassName().equals( event.getCurrentDataObject().getClassName() ) ) {

            //check deleted object is referenced by current data object.
            if ( validatorService.isReferencedByCurrentObject( event.getCurrentDataObject(), context.getDataObject() ) ) {
                notification.fire( new NotificationEvent( Constants.INSTANCE.modelEditor_notification_dataObject_referenced_has_been_deleted( event.getCurrentDataObject().getClassName(), context.getDataObject().getClassName() ) ) );
            } else if ( !getDataModel().isExternal( event.getCurrentDataObject().getClassName() ) ) {
                getDataModel().removeDataObject( event.getCurrentDataObject().getClassName() );
                view.refreshTypeLists( true );
            }
        }
    }

    private void onDataObjectCreated( @Observes DataObjectCreatedEvent event ) {
        if ( context != null &&
                event.isFrom( context.getCurrentProject() ) &&
                event.getCurrentDataObject() != null &&
                getDataModel() != null &&
                getDataModel().getDataObject( event.getCurrentDataObject().getClassName() ) == null ) {
            getDataModel().addDataObject( event.getCurrentDataObject() );
            view.refreshTypeLists( true );
        }
    }

    private void onDataObjectFieldChangeEvent( @Observes DataObjectFieldChangeEvent event ) {
        if ( isFromThisContext( event ) ) {
            notifyLock( );
            refreshTitle( event.getCurrentDataObject(), event.getCurrentField() );
            updateChangeStatus( event );
        }
    }

    private void onDataObjectFieldDeleted( @Observes DataObjectFieldDeletedEvent event ) {
        if ( isFromThisContext( event ) ) {
            updateChangeStatus( event );
        }
    }

    protected void onDataObjectChangeEvent( @Observes DataObjectChangeEvent event ) {
        if ( isFromThisContext( event ) ) {
            notifyLock( );
            refreshTitle( event.getCurrentDataObject() );
            updateChangeStatus( event );
        }
    }

    private void onDataObjectFieldCreated( @Observes DataObjectFieldCreatedEvent event ) {
        if ( isFromThisContext( event ) ) {
            updateChangeStatus( event );
        }
    }

    protected void onDataObjectSelectedEvent( @Observes DataObjectSelectedEvent event ) {
        if ( isFromThisContext( event ) ) {
            refreshTitle( event.getCurrentDataObject() );
        }
    }

    protected void onDataObjectFieldSelectedEvent( @Observes DataObjectFieldSelectedEvent event ) {
        if ( isFromThisContext( event ) ) {
            refreshTitle( event.getCurrentDataObject(), event.getCurrentField() );
        }
    }

    private void notifyLock( ) {
        lockRequired.fire( new LockRequiredEvent() );
    }

    private void cleanSystemMessages( String currentMessageType ) {
        UnpublishMessagesEvent unpublishMessage = new UnpublishMessagesEvent();
        unpublishMessage.setShowSystemConsole( false );
        unpublishMessage.setMessageType( currentMessageType );
        unpublishMessage.setUserId( ( sessionInfo != null && sessionInfo.getIdentity() != null ) ? sessionInfo.getIdentity().getIdentifier() : null );
        unpublishMessagesEvent.fire( unpublishMessage );
    }

    private void publishSystemMessages( String messageType,
                                        boolean cleanExisting,
                                        List<DataModelerError> errors ) {
        PublishBatchMessagesEvent publishMessage = new PublishBatchMessagesEvent();
        publishMessage.setCleanExisting( cleanExisting );
        publishMessage.setMessageType( messageType );
        publishMessage.setUserId( ( sessionInfo != null && sessionInfo.getIdentity() != null ) ? sessionInfo.getIdentity().getIdentifier() : null );
        publishMessage.setPlace( PublishBaseEvent.Place.TOP );
        SystemMessage systemMessage;
        for ( DataModelerError error : errors ) {
            systemMessage = new SystemMessage();
            systemMessage.setMessageType( messageType );
            systemMessage.setId( error.getId() );
            systemMessage.setText( error.getMessage() );
            systemMessage.setPath( error.getFile() );
            systemMessage.setLevel( error.getLevel() );
            systemMessage.setLine( error.getLine() );
            systemMessage.setColumn( error.getColumn() );
            publishMessage.getMessagesToPublish().add( systemMessage );
        }
        publishBatchMessagesEvent.fire( publishMessage );
    }

    protected void makeMenuBar() {

        //menus =
        menuBuilder
                .addSave( versionRecordManager.newSaveMenuItem( new Command() {
                    @Override
                    public void execute() {
                        onSave();
                    }
                } ) )
                .addCopy( new Command() {
                    @Override
                    public void execute() {
                        onCopy();
                    }
                } )
                .addRename( new Command() {
                    @Override
                    public void execute() {
                        onSafeRename();
                    }
                } )
                .addDelete( new Command() {
                    @Override
                    public void execute() {
                        onSafeDelete();
                    }
                } )
                .addValidate(
                        onValidate()
                            )
                .addNewTopLevelMenu( versionRecordManager.buildMenu() );
        menus = menuBuilder.build();
    }

    private void clearContext() {
        context.clear();
    }

    private String getCurrentMessageType() {
        return currentMessageType;
    }

    private boolean isFromThisContext( DataModelerEvent event ) {
        return event.isFromContext( context != null ? context.getContextId() : null );
    }

    private void refreshTitle( DataObject dataObject ) {
        if ( dataObject != null ) {
            String label = DataModelerUtils.getDataObjectFullLabel( dataObject, false );
            String title = "'" + label + "'" + Constants.INSTANCE.modelEditor_general_properties_label();
            String tooltip = dataObject.getClassName();
            view.setDomainContainerTitle( title, tooltip );
        }
    }

    private void refreshTitle( DataObject dataObject, ObjectProperty objectProperty ) {
        if ( dataObject != null && objectProperty != null ) {
            String title = "'" + objectProperty.getName() + "'" + Constants.INSTANCE.modelEditor_general_properties_label();
            String tooltip = dataObject.getClassName() + "." + objectProperty.getName();
            view.setDomainContainerTitle( title, tooltip );
        }
    }

    private void updateChangeStatus( DataModelerEvent event ) {
        if ( isFromThisContext( event ) ) {
            context.setEditionStatus( DataModelerContext.EditionStatus.EDITOR_CHANGED );
            dataModelerEvent.fire( new DataModelStatusChangeEvent( context.getContextId(), null, false, true ) );
        }
    }

}