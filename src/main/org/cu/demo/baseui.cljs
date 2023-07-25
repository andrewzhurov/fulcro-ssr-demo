(ns org.cu.demo.baseui
  (:require
   [com.fulcrologic.fulcro.components :as c]
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop :refer [react-factory]]
   ["baseui" :refer [BaseProvider LightTheme DarkTheme ThemeProvider]]
   ["baseui/form-control" :refer [FormControl]]
   ["baseui/input" :refer [Input]]
   ["baseui/textarea" :refer [Textarea]]
   ["baseui/button" :refer [Button SIZE]]
   ["baseui/card" :refer [Card StyledBody StyledAction]]
   ["baseui/block" :refer [Block]]
   ["baseui/heading" :refer [HeadingLevel Heading]]
   ["baseui/dnd-list" :refer [arrayMove arrayRemove StatefulList] :rename {List DNDList}]
   ["baseui/list" :refer [List ListHeading]]
   ["baseui/app-nav-bar" :refer [AppNavBar setItemActive NavItem]]
   ["baseui/header-navigation" :refer [HeaderNavigation ALIGN StyledNavigationList StyledNavigationItem]]
   ["baseui/avatar" :refer [Avatar]]
   ["baseui/modal" :refer [Modal ModalHeader ModalBody ModalFooter ModalButton FocusOnce SIZE ROLE] :rename {SIZE ModalSIZE}]
   ))

(def ui-modal (react-factory Modal))
(def ui-modal-header (react-factory ModalHeader))
(def ui-modal-body (react-factory ModalBody))
(def ui-modal-footer (react-factory ModalFooter))
(def ui-modal-button (react-factory ModalButton))
(def ui-modal-focus-once FocusOnce)
(def modal-size ModalSIZE)
(def modal-role ROLE)

(def ui-header-nav (react-factory HeaderNavigation))
(def ui-header-nav-list (react-factory StyledNavigationList))
(def ui-header-nav-item (react-factory StyledNavigationItem))
(def header-nav-list-align ALIGN)

(def ui-base-provider (react-factory BaseProvider))
(def ui-theme-provider (react-factory ThemeProvider))
(def light-theme LightTheme)
(def dark-theme DarkTheme)

(def ui-form-control (react-factory FormControl))
(def ui-input (react-factory Input))

(def ui-text-area (react-factory Textarea))

(def ui-button (react-factory Button))
(def button-size SIZE)

(def ui-card (react-factory Card))
(def ui-card-styled-body (react-factory StyledBody))
(def ui-card-styled-action (react-factory StyledAction))

(def ui-block (react-factory Block))

(def ui-heading-level (react-factory HeadingLevel))
(def ui-heading (react-factory Heading))

(def ui-dnd-list (react-factory DNDList))
(def ui-dnd-list-stateful (react-factory StatefulList))

(def ui-list-heading (react-factory ListHeading))

(def ui-navbar (react-factory AppNavBar))
(def ui-navbar-item (react-factory NavItem))

(def ui-avatar (react-factory Avatar))
