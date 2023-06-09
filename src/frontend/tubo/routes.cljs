(ns tubo.routes
  (:require
   [reitit.frontend :as ref]
   [reitit.frontend.easy :as rfe]
   [re-frame.core :as rf]
   [tubo.events :as events]
   [tubo.views.channel :as channel]
   [tubo.views.kiosk :as kiosk]
   [tubo.views.playlist :as playlist]
   [tubo.views.search :as search]
   [tubo.views.stream :as stream]))

(def routes
  (ref/router
   [["/" {:view kiosk/kiosk
          :name ::home
          :controllers [{:start (fn [_]
                                  (rf/dispatch [::events/change-service-id 0])
                                  (rf/dispatch [::events/get-default-kiosk 0])
                                  (rf/dispatch [::events/get-kiosks 0]))}]}]
    ["/search" {:view search/search
                :name ::search
                :controllers [{:parameters {:query [:q :serviceId]}
                               :start (fn [{{:keys [serviceId q]} :query}]
                                        (rf/dispatch [::events/change-service-id (js/parseInt serviceId)])
                                        (rf/dispatch [::events/get-search-results serviceId q]))}]}]
    ["/stream" {:view stream/stream
                :name ::stream
                :controllers [{:parameters {:query [:url]}
                               :start (fn [{{:keys [url]} :query}]
                                        (rf/dispatch [::events/get-stream url]))}]}]
    ["/channel" {:view channel/channel
                 :name ::channel
                 :controllers [{:parameters {:query [:url]}
                                :start (fn [{{:keys [url]} :query}]
                                         (rf/dispatch [::events/get-channel url]))}]}]
    ["/playlist" {:view playlist/playlist
                  :name ::playlist
                  :controllers [{:parameters {:query [:url]}
                                 :start (fn [{{:keys [url]} :query}]
                                          (rf/dispatch [::events/get-playlist url]))}]}]
    ["/kiosk" {:view kiosk/kiosk
               :name ::kiosk
               :controllers [{:parameters {:query [:kioskId :serviceId]}
                              :start (fn [{{:keys [serviceId kioskId]} :query}]
                                       (rf/dispatch [::events/change-service-id (js/parseInt serviceId)])
                                       (rf/dispatch [::events/get-kiosk serviceId kioskId])
                                       (rf/dispatch [::events/get-kiosks serviceId]))}]}]]))

(defn on-navigate
  [new-match]
  (rf/dispatch [::events/reset-page-scroll])
  (when new-match
    (rf/dispatch [::events/navigated new-match])))

(defn start-routes!
  []
  (rfe/start! routes on-navigate {:use-fragment false}))
