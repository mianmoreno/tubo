(ns tau.api.channel
  (:require
   [tau.api.stream :as stream]
   [tau.api.playlist :as playlist]
   [clojure.java.data :as j]
   [ring.util.codec :refer [url-decode]])
  (:import
   org.schabi.newpipe.extractor.channel.ChannelInfo
   org.schabi.newpipe.extractor.NewPipe
   org.schabi.newpipe.extractor.Page))

(defrecord Channel
    [id name description verified? banner avatar
     subscriber-count donation-links next-page
     related-streams])

(defrecord ChannelResult
    [name description verified? thumbnail-url url
     subscriber-count stream-count])

(defrecord ChannelPage
    [next-page related-streams])

(defn get-results
  [items]
  (map #(case (.name (.getInfoType %))
          "STREAM" (stream/get-result %)
          "CHANNEL" (get-result %)
          "PLAYLIST" (playlist/get-result %))
       items))

(defn get-result
  [channel]
  (map->ChannelResult
   {:name (.getName channel)
    :thumbnail-url (.getThumbnailUrl channel)
    :url (.getUrl channel)
    :description (.getDescription channel)
    :subscriber-count (.getSubscriberCount channel)
    :stream-count (.getStreamCount channel)
    :verified? (.isVerified channel)}))

(defn get-info
  ([url]
   (let [info (ChannelInfo/getInfo (url-decode url))]
     (map->Channel
      {:id (.getId info)
       :name (.getName info)
       :verified? (.isVerified info)
       :banner (.getBannerUrl info)
       :avatar (.getAvatarUrl info)
       :subscriber-count (.getSubscriberCount info)
       :donation-links (.getDonationLinks info)
       :next-page (j/from-java (.getNextPage info))
       :related-streams (get-results (.getRelatedItems info))})))
  ([url page-url]
   (let [service (NewPipe/getServiceByUrl (url-decode url))
         info (ChannelInfo/getMoreItems service url (Page. (url-decode page-url)))]
     (map->ChannelPage
      {:related-streams (get-results (.getItems info))
       :next-page (j/from-java (.getNextPage info))}))))
