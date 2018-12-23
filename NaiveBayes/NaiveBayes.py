import sys
from scipy.sparse import csr_matrix
import numpy as np
from Eval import Eval
from math import log, exp
import time
from imdb import IMDBdata
from sklearn.metrics import confusion_matrix

class NaiveBayes:
    def __init__(self, data, ALPHA=5.0):
        self.ALPHA = ALPHA
        self.data = data  # training data
        # TODO: Initalize parameters
        self.vocab_len = data.X.shape[1]
        self.count_positive = np.zeros([1, data.X.shape[1]])
        self.count_negative = np.zeros([1, data.X.shape[1]])
        self.num_positive_reviews = 0
        self.num_negative_reviews = 0
        self.total_positive_words = 0
        self.total_negative_words = 0
        self.P_positive = 0.0
        self.P_negative = 0.0
        self.deno_pos = 0.0
        self.deno_neg = 0.0
        self.Train(data.X, data.Y)

    def Train(self, X, Y):
        # TODO: Estimate Naive Bayes model parameters
        positive_indices = np.argwhere(Y == 1.0).flatten()
        negative_indices = np.argwhere(Y == -1.0).flatten()

        self.num_positive_reviews = len(positive_indices)
        self.num_negative_reviews = len(negative_indices)

        self.count_positive = csr_matrix.sum(X[np.ix_(positive_indices)], axis=0)
        self.count_negative = csr_matrix.sum(X[np.ix_(negative_indices)], axis=0)

        self.total_positive_words = csr_matrix.sum(X[np.ix_(positive_indices)])
        self.total_negative_words = csr_matrix.sum(X[np.ix_(negative_indices)])

        self.deno_pos = float(self.total_positive_words + self.ALPHA * X.shape[1])
        self.deno_neg = float(self.total_negative_words + self.ALPHA * X.shape[1])

        return

    def PredictLabel(self, X):
        # TODO: Implement Naive Bayes Classification
        self.P_positive_review = (float(self.num_positive_reviews)/(self.num_positive_reviews + self.num_negative_reviews))
        self.P_negative_review = (float(self.num_negative_reviews)/(self.num_positive_reviews + self.num_negative_reviews))
        pred_labels = []

        sh = X.shape[0]
        for i in range(sh):

            z = X[i].nonzero()

            Positive_label_val = log(self.P_positive_review)
            Negative_label_val = log(self.P_negative_review)

            for j in range(len(z[0])):
                rowvalue = X[i,z[1][j]]

                Positive_val = log(self.count_positive[0, z[1][j]]+ self.ALPHA) - log(self.deno_pos)
                Positive_label_val = Positive_label_val + rowvalue * Positive_val

                Negative_val = log(self.count_negative[0, z[1][j]]+ self.ALPHA) - log(self.deno_neg)
                Negative_label_val = Negative_label_val + rowvalue * Negative_val

            # print(self.Positive_label_val)
            # print(self.Negative_label_val)

            if Positive_label_val > Negative_label_val:
                pred_labels.append(1.0)
            else:
                pred_labels.append(-1.0)

        return pred_labels
        # return 1

    def LogSum(self, logx, logy):
        # TO Do: Return log(x+y), avoiding numerical underflow/overflow.
        m = max(logx, logy)
        return m + log(exp(logx - m) + exp(logy - m))

    def PredictProb(self, test, indexes):

        for i in indexes:
            # TO DO: Predict the probability of the i_th review in test being positive review
            # TO DO: Use the LogSum function to avoid underflow/overflow
            predicted_label = 0
            z = test.X[i].nonzero()
            positive_val = self.P_positive_review
            negative_val = self.P_negative_review
            for j in range(len(z[0])):
                datavalue = test.X[i,z[1][j]]

                predicted_negative_value = log((self.count_negative[0, z[1][j]]+self.ALPHA))
                negative_val = negative_val + datavalue * predicted_negative_value

                predicted_positive_value = log((self.count_positive[0, z[1][j]]+self.ALPHA))
                positive_val = positive_val + datavalue * predicted_positive_value

            predicted_prob_positive = exp(predicted_positive_value - self.LogSum(predicted_positive_value,predicted_negative_value))
            predicted_prob_negative = exp(predicted_negative_value - self.LogSum(predicted_positive_value,predicted_negative_value))

            if predicted_positive_value > predicted_negative_value:
                predicted_label = 1.0
            else:
                predicted_label = -1.0

            # print test.Y[i], test.X_reviews[i]
            # TO DO: Comment the line above, and uncomment the line below
            print(test.Y[i], predicted_label, predicted_prob_positive, predicted_prob_negative, test.X_reviews[i])

    def Eval(self, test):
        Y_pred = self.PredictLabel(test.X)
        ev = Eval(Y_pred, test.Y)
        return ev.Accuracy()

    def Evalprecision(self, test):
        Y_pred = self.PredictLabel(test.X)
        #confmat = confusion_matrix(test.Y,Y_pred)
        tn, fp, fn, tp = confusion_matrix(test.Y,Y_pred).ravel()
        print(tn,fp,fn,tp)
       # tn, fp, fn, tp = confmat.ravel()
        precision_val= tp/float(tp + fp)
        print (precision_val)


    def EvalRecall(self, test):
        Y_pred = self.PredictLabel(test.X)
        tn, fp, fn, tp = confusion_matrix(test.Y,Y_pred).ravel()
        recall_val = tp / float(tp + fn)
        print(recall_val)


if __name__ == "__main__":
    print("Reading Training Data")
    traindata = IMDBdata("%s/train" % sys.argv[1])
    print("Reading Test Data")
    testdata = IMDBdata("%s/test" % sys.argv[1], vocab=traindata.vocab)
    print("Computing Parameters")
    nb = NaiveBayes(traindata, float(sys.argv[2]))
    print("Evaluating")
    print("Test Accuracy: ", nb.Eval(testdata))
    print(nb.PredictProb(testdata, range(10)))
    #print("Precision :" , nb.Evalprecision(testdata))
    #print("Recall :" , nb.EvalRecall(testdata))