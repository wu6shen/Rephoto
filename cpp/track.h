#ifndef _TRACK_H_
#define _TRACK_H_

#include "common_include.h"
#include "frame.h"
#include "orb.h"

namespace reloc {
    class Track {
    public:
        long cur_id_;
        
        Frame ori_frame_;
        Frame ori_frame_old_;
        Frame ref_frame_;
        Frame last_frame_;
        Frame cur_frame_;
        Frame cur_frame_old_;
        Frame stack[1000];
        Frame match_;
        int top;
        int method_;
        
        cv::Mat last_img_, ori_img_, cur_img_;
        cv::Ptr<cv::ORB> orb_ori_;
        cv::Ptr<cv::ORB> orb_com_;
        ORBextractor *orb_ori_t_;
        ORBextractor *orb_com_t_;
        ORBextractor *orb_com_old_t_;

        cv::Point2f center_;
        double rect_scale_;
        double img_scale_;
        
        int isJ_;
        
        int inlier_num_[3];
        int down_num_;
        int up_num_;
        int now_model_;
        int lk_num_;
        int lk_init_num_;
        bool lk_model_;

        int in_num_, out_num_;

        double score_;
        
        Track();
        Track(cv::Mat &img, int isJ, int method);

        void clear();
        
        cv::Mat tracking(cv::Mat &img);
        void trackUseKeyPoint(cv::Mat &img);
        void trackUseKeyPointToOld(cv::Mat &img);
        void trackUseLK(cv::Mat &img);
        
        void changeModel();
        
        void getMatchUseKnn(Frame &f1, Frame &f2, std::vector<cv::DMatch> &matches);
        void updateMatch(Frame &f1, Frame &f2, std::vector<cv::DMatch> &matches);
        void computeHomo(Frame &f1, Frame &f2, cv::Mat &h);
        
        void getDisToOriUseRect(cv::Mat &img, Frame &f1);

        double getScore();
        
        cv::Mat drawInfo(cv::Mat &img, Frame &f, const cv::Scalar &color);
        cv::Mat drawMatch(cv::Mat &img, Frame &f);
        std::vector<int> num_p_;
        void updateD(int id, uchar *desc);
    };
}

#endif
